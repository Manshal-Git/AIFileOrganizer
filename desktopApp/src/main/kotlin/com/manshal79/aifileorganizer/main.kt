package com.manshal79.aifileorganizer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.manshal79.aifileorganizer.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "AIFileOrganizer",
    ) {
        App()
    }
}