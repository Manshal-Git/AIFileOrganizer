package com.manshal79.aifileorganizer.di

import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import com.manshal79.aifileorganizer.domain.usecase.CheckOllamaAvailabilityUseCase
import com.manshal79.aifileorganizer.domain.usecase.RenameSuggestionUseCase
import com.manshal79.aifileorganizer.presentation.organizer.OrganizerViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val ollamaModel = LLModel(
    provider = LLMProvider.Ollama,
    id = "gemma3:4b", // Exact name from `ollama list`
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Basic,
        LLMCapability.Tools,
        LLMCapability.Vision.Image,
    ),
    contextLength = 40_960,
)

val appModule = module {
    single { ollamaModel }
    single { OllamaClient() }
    single<PromptExecutor> { MultiLLMPromptExecutor(get<OllamaClient>()) }
    singleOf(::RenameSuggestionUseCase)
    singleOf(::CheckOllamaAvailabilityUseCase)
    viewModelOf(::OrganizerViewModel)
}
