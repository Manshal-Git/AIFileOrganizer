package com.manshal79.aifileorganizer.domain.usecase

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import com.manshal79.aifileorganizer.domain.model.ExtractedContent
import com.manshal79.aifileorganizer.domain.model.ModelOutputException
import com.manshal79.aifileorganizer.domain.model.RenameSuggestion
import com.manshal79.aifileorganizer.domain.model.RenameSuggestionResult
import com.manshal79.aifileorganizer.domain.model.TokenUsage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.files.Path
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

class RenameSuggestionUseCase(
    private val promptExecutor: PromptExecutor,
) {
    /**
     * @param thinking whether the model should reason before answering, or null to say nothing
     *   about it — pass null for models that don't report the `thinking` capability, since Ollama
     *   rejects the flag outright for those.
     */
    suspend fun suggest(
        fileName: String,
        content: ExtractedContent,
        model: LLModel,
        thinking: Boolean? = null,
    ): RenameSuggestionResult {
        val (result, duration) = measureTimedValue {
            promptExecutor.executeStructured<RenameSuggestion>(
                prompt = prompt("rename-suggestion", params = thinkingParams(thinking)) {
                    system(
                        "You rename files based on their content which makes them easier to be retrieved/searched later. " +
                            "The content may be given as text, or as an image if the file is a photo, screenshot, or " +
                            "scanned document. Suggest a short, descriptive filename (no extension, snake_case, no " +
                            "special characters) and a short category for it."
                    )
                    user {
                        text("Current filename: $fileName")
                        when (content) {
                            is ExtractedContent.Text -> text("\n\nContent:\n${content.text}")
                            is ExtractedContent.Image -> image(Path(content.path))
                        }
                    }
                },
                model = model,
                examples = listOf(
                    RenameSuggestion(suggestedName = "q3_sales_report", category = "report"),
                    RenameSuggestion(suggestedName = "jane_doe_resume", category = "resume"),
                ),
            )
        }

        val response = result.getOrElse { error ->
            throw error.logged(fileName = fileName, model = model, thinking = thinking)
        }
        // No StructureFixingParser is passed above, so this is exactly one call to the model
        // and its metaInfo accounts for the whole request. Adding a fixing parser later would
        // mean extra calls whose usage never reaches this response.
        val metaInfo = response.message.metaInfo

        return RenameSuggestionResult(
            suggestion = response.data,
            usage = TokenUsage(
                inputTokens = metaInfo.inputTokensCount ?: 0,
                outputTokens = metaInfo.outputTokensCount ?: 0,
            ),
            durationMillis = duration.inWholeMilliseconds,
        )
    }

    /**
     * Logs everything needed to reproduce the failure — which file, which model, whether thinking
     * was on — and returns what should be thrown in its place.
     *
     * A [SerializationException] here means the model answered with something that isn't the
     * requested JSON. kotlinx puts the offending input in its own message, so the log carries the
     * actual malformed output; the caller gets a [ModelOutputException] whose message is safe to
     * show a user.
     */
    private fun Throwable.logged(fileName: String, model: LLModel, thinking: Boolean?): Throwable {
        val context = "file='$fileName', model=${model.id}, thinking=$thinking"
        return when (this) {
            is SerializationException -> {
                logger.error(this) { "Unparsable model output for rename suggestion ($context)" }
                ModelOutputException(modelId = model.id, cause = this)
            }

            else -> {
                logger.error(this) { "Rename suggestion request failed ($context)" }
                this
            }
        }
    }

    /**
     * Ollama's own `think` flag lives on Koog's `OllamaParams`, but it never survives a structured
     * call: `executeStructured` rebuilds the prompt params through `Prompt.withUpdatedParams` →
     * `LLMParams.copy`, which returns a plain `LLMParams` and drops the Ollama subtype. Extra
     * properties do survive that copy, and `OllamaClient` flattens them into the request root
     * (`AdditionalPropertiesFlatteningSerializer`), so the flag still lands as a top-level
     * `"think"` field on `/api/chat`.
     */
    private fun thinkingParams(thinking: Boolean?): LLMParams = when (thinking) {
        null -> LLMParams()
        else -> LLMParams(additionalProperties = mapOf("think" to JsonPrimitive(thinking)))
    }
}
