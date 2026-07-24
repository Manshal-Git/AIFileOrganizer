package com.manshal79.aifileorganizer.di

import com.manshal79.aifileorganizer.Agent
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::Agent)
}
