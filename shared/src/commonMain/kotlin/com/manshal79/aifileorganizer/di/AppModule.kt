package com.manshal79.aifileorganizer.di

import com.manshal79.aifileorganizer.Agent
import com.manshal79.aifileorganizer.presentation.organizer.OrganizerViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::Agent)
    viewModelOf(::OrganizerViewModel)
}
