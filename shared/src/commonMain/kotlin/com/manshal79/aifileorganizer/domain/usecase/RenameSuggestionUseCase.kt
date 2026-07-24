package com.manshal79.aifileorganizer.domain.usecase

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.llm.LLModel
import com.manshal79.aifileorganizer.domain.model.RenameSuggestion

class RenameSuggestionUseCase(
    private val promptExecutor: PromptExecutor,
    private val model: LLModel,
) {
    suspend fun suggest(fileName: String, content: String): RenameSuggestion {
        val result = promptExecutor.executeStructured<RenameSuggestion>(
            prompt = prompt("rename-suggestion") {
                system(
                    "You rename files based on their content which makes them easier to be retrieved/searched later. Suggest a short, descriptive filename " +
                        "(no extension, snake_case, no special characters) and a short category for it."
                )
                user("Current filename: $fileName\n\nContent:\n$content")
            },
            model = model,
            examples = listOf(
                RenameSuggestion(suggestedName = "q3_sales_report", category = "report"),
                RenameSuggestion(suggestedName = "jane_doe_resume", category = "resume"),
            ),
        )

        return result.getOrThrow().data
    }
}
