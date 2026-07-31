package com.manshal79.aifileorganizer.domain.model

import ai.koog.prompt.executor.ollama.client.OllamaModelCard

/**
 * A pulled Ollama model as this app sees it: Koog's card plus the one capability Koog throws
 * away while parsing `/api/show`.
 */
data class OllamaModelInfo(
    val card: OllamaModelCard,
    val supportsThinking: Boolean,
)
