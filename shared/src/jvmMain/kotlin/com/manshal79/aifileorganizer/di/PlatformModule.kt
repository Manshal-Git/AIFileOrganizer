package com.manshal79.aifileorganizer.di

import com.manshal79.aifileorganizer.data.filesystem.FileScanner
import com.manshal79.aifileorganizer.data.filesystem.JvmFileScanner
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule = module {
    singleOf(::JvmFileScanner) { bind<FileScanner>() }
}
