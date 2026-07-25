package com.manshal79.aifileorganizer.domain.usecase

import ai.koog.prompt.executor.ollama.client.OllamaClient

class CheckOllamaAvailabilityUseCase(
    private val ollamaClient: OllamaClient,
) {
    suspend fun isAvailable(): Boolean = try {
        ollamaClient.getModels()
        true
    } catch (e: Exception) {
        false
    }
}
