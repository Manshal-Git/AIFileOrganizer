package com.manshal79.aifileorganizer.domain.usecase

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.llm.LLModel
import com.manshal79.aifileorganizer.domain.model.ExtractedContent
import com.manshal79.aifileorganizer.domain.model.RenameSuggestion
import com.manshal79.aifileorganizer.domain.model.RenameSuggestionResult
import com.manshal79.aifileorganizer.domain.model.TokenUsage
import kotlinx.io.files.Path

class RenameSuggestionUseCase(
    private val promptExecutor: PromptExecutor,
    private val model: LLModel,
) {
    suspend fun suggest(
        fileName: String,
        content: ExtractedContent
    ): RenameSuggestionResult {
        val result = promptExecutor.executeStructured<RenameSuggestion>(
            prompt = prompt("rename-suggestion") {
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

        val response = result.getOrThrow()
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
        )
    }
}
