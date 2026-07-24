package com.manshal79.aifileorganizer

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.llms.RoutingLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

class Agent {
    // Create an agent
    val agent by lazy {

        val customOllamaModel = LLModel(
            provider = LLMProvider.Ollama,
            id = "gemma3:4b", // Exact name from `ollama list`
            capabilities = listOf(
                LLMCapability.Temperature,
                LLMCapability.Schema.JSON.Basic,
                LLMCapability.Tools // Add if your model supports function calling
            ),
            contextLength = 40_960
        )

        AIAgent(
            promptExecutor = MultiLLMPromptExecutor(OllamaClient()),
            llmModel = customOllamaModel
        )
    }

    // Run the agent
    suspend fun ask(
        prompt: String
    ): String {
        return try {
            agent.run(
                agentInput = prompt
            )
        } catch (e: Exception) {
            "Error: Could not connect to Ollama. Please ensure it is running on http://localhost:11434. (${e.message})"
        }
    }
}