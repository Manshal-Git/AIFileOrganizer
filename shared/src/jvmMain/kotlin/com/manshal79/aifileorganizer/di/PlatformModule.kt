package com.manshal79.aifileorganizer.di

import com.manshal79.aifileorganizer.data.content.JvmTextContentExtractor
import com.manshal79.aifileorganizer.data.content.TextContentExtractor
import com.manshal79.aifileorganizer.data.filesystem.FileHasher
import com.manshal79.aifileorganizer.data.filesystem.FileRenamer
import com.manshal79.aifileorganizer.data.filesystem.FileScanner
import com.manshal79.aifileorganizer.data.filesystem.JvmFileHasher
import com.manshal79.aifileorganizer.data.filesystem.JvmFileRenamer
import com.manshal79.aifileorganizer.data.filesystem.JvmFileScanner
import com.manshal79.aifileorganizer.data.ollama.JvmOllamaCapabilityProbe
import com.manshal79.aifileorganizer.data.ollama.OllamaCapabilityProbe
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule = module {
    singleOf(::JvmFileScanner) { bind<FileScanner>() }
    singleOf(::JvmTextContentExtractor) { bind<TextContentExtractor>() }
    singleOf(::JvmFileRenamer) { bind<FileRenamer>() }
    singleOf(::JvmFileHasher) { bind<FileHasher>() }
    single<OllamaCapabilityProbe> { JvmOllamaCapabilityProbe() }
}
