package com.manshal79.aifileorganizer.domain.usecase

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.params.LLMParams
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Guards the reason [RenameSuggestionUseCase] smuggles Ollama's `think` flag through
 * `additionalProperties` instead of `OllamaParams.think`: the structured-output path rewrites
 * prompt params, and only one of those two survives the rewrite. If a Koog upgrade ever makes the
 * subtype survive, this test still passes — but the day `additionalProperties` stops surviving,
 * thinking would silently stop toggling, and this is what catches it.
 */
class ThinkingParamTest {

    @Test
    fun `additional properties survive the structured-output param rewrite`() {
        val original = prompt(
            "rename-suggestion",
            params = LLMParams(additionalProperties = mapOf("think" to JsonPrimitive(false))),
        ) {
            user("hi")
        }

        // What StructuredRequest.updatePrompt does before a native structured-output call.
        val rewritten = original.withUpdatedParams { schema = null }

        assertEquals(JsonPrimitive(false), rewritten.params.additionalProperties?.get("think"))
    }

    @Test
    fun `no think property is sent when the model cannot think`() {
        val prompt = prompt("rename-suggestion", params = LLMParams()) { user("hi") }

        assertNull(prompt.params.additionalProperties?.get("think"))
    }
}
