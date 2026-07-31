package com.manshal79.aifileorganizer.data.ollama

/**
 * Reads the raw `capabilities` list Ollama reports for a model from `/api/show`.
 *
 * Koog already calls that endpoint for [ai.koog.prompt.executor.ollama.client.OllamaModelCard],
 * but its converter maps `thinking` to nothing at all
 * (`OllamaManagementConverters.toLLMCapabilities`), so the flag never reaches the card. This probe
 * exists purely to recover the capability names Koog drops.
 */
interface OllamaCapabilityProbe {
    /** Lowercase capability names, e.g. `completion`, `vision`, `tools`, `thinking`. */
    suspend fun capabilities(modelName: String): Set<String>
}
