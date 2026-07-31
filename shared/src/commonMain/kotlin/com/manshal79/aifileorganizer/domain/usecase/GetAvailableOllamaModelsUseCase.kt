package com.manshal79.aifileorganizer.domain.usecase

import ai.koog.prompt.executor.ollama.client.OllamaClient
import com.manshal79.aifileorganizer.data.ollama.OllamaCapabilityProbe
import com.manshal79.aifileorganizer.domain.model.OllamaModelInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private val logger = KotlinLogging.logger {}

/** Lists the models actually pulled into the local Ollama install, for the user to pick from. */
class GetAvailableOllamaModelsUseCase(
    private val ollamaClient: OllamaClient,
    private val capabilityProbe: OllamaCapabilityProbe,
) {
    // Capabilities are fixed for a pulled model, so probe each name once per session instead of
    // on every refresh/scan — the list is refetched often and each miss is another HTTP round trip.
    private val thinkingSupportCache = mutableMapOf<String, Boolean>()

    suspend fun getModels(): Result<List<OllamaModelInfo>> = runCatching {
        val cards = ollamaClient.getModels()
        coroutineScope {
            cards
                .map { card -> async { OllamaModelInfo(card, supportsThinking(card.name)) } }
                .awaitAll()
        }
    }

    /**
     * A failed probe reports "no thinking" rather than failing the whole list — losing one
     * optional toggle beats an empty model picker. That answer isn't cached, so the next
     * refresh retries it.
     */
    private suspend fun supportsThinking(modelName: String): Boolean {
        thinkingSupportCache[modelName]?.let { return it }
        return runCatching { THINKING_CAPABILITY in capabilityProbe.capabilities(modelName) }
            .onSuccess { thinkingSupportCache[modelName] = it }
            .onFailure { logger.warn(it) { "Capability probe failed for '$modelName'" } }
            .getOrDefault(false)
    }

    private companion object {
        const val THINKING_CAPABILITY = "thinking"
    }
}
