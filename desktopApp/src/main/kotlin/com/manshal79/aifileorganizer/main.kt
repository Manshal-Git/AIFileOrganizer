package com.manshal79.aifileorganizer

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.manshal79.aifileorganizer.di.appModule
import com.manshal79.aifileorganizer.di.platformModule
import org.koin.core.context.startKoin
import java.awt.Dimension

fun main() {
    // Start Koin once, before entering composition (not inside application {},
    // which would re-run it on every recomposition).
    startKoin {
        modules(appModule, platformModule)
    }

    application {
        val windowState = rememberWindowState(size = DpSize(1100.dp, 760.dp))
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "AIFileOrganizer",
        ) {
            // Keep the layout usable — don't let the window shrink below what the UI needs.
            window.minimumSize = Dimension(900, 600)
            App()
        }
    }
}
