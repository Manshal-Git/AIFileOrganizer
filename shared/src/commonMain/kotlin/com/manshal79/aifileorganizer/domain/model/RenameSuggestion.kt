package com.manshal79.aifileorganizer.domain.model

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@Serializable
@LLMDescription("A suggested filename and category for a document, based on its content")
data class RenameSuggestion(
    @property:LLMDescription(
        "A short, descriptive filename without extension, using snake_case, no special characters"
    )
    val suggestedName: String,
    @property:LLMDescription("A one or two word category for the document, e.g. invoice, resume, report, letter")
    val category: String,
)
