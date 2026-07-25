package com.manshal79.aifileorganizer.domain.usecase

import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.executor.ollama.client.OllamaModelCard

/** Lists the models actually pulled into the local Ollama install, for the user to pick from. */
class GetAvailableOllamaModelsUseCase(
    private val ollamaClient: OllamaClient,
) {
    suspend fun getModels(): Result<List<OllamaModelCard>> = runCatching { ollamaClient.getModels() }
}
