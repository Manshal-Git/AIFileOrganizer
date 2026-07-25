package com.manshal79.aifileorganizer.di

import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import com.manshal79.aifileorganizer.domain.usecase.GetAvailableOllamaModelsUseCase
import com.manshal79.aifileorganizer.domain.usecase.RenameSuggestionUseCase
import com.manshal79.aifileorganizer.presentation.organizer.OrganizerViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { OllamaClient() }
    single<PromptExecutor> { MultiLLMPromptExecutor(get<OllamaClient>()) }
    singleOf(::RenameSuggestionUseCase)
    singleOf(::GetAvailableOllamaModelsUseCase)
    viewModelOf(::OrganizerViewModel)
}
