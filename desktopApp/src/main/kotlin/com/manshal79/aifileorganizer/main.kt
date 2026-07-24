package com.manshal79.aifileorganizer

import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manshal79.aifileorganizer.di.appModule
import com.manshal79.aifileorganizer.di.platformModule
import com.manshal79.aifileorganizer.presentation.organizer.FileViewMode
import com.manshal79.aifileorganizer.presentation.organizer.OrganizerAction
import com.manshal79.aifileorganizer.presentation.organizer.OrganizerViewModel
import com.manshal79.aifileorganizer.presentation.organizer.pickDirectory
import org.koin.compose.viewmodel.koinViewModel
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

            // Same koinViewModel() instance OrganizerRoot resolves below (single
            // ViewModelStoreOwner for the app) — the menu bar and screen stay in sync.
            val viewModel: OrganizerViewModel = koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            MenuBar {
                Menu("File") {
                    Item(
                        "Choose Folder…",
                        onClick = {
                            pickDirectory()?.let { path ->
                                viewModel.onAction(OrganizerAction.OnFolderSelected(path))
                            }
                        },
                    )
                    Item(
                        "Rescan",
                        enabled = state.folderPath != null,
                        onClick = { viewModel.onAction(OrganizerAction.OnRescanClick) },
                    )
                    Separator()
                    Item("Exit", onClick = ::exitApplication)
                }
                Menu("View") {
                    RadioButtonItem(
                        "List",
                        selected = state.viewMode == FileViewMode.LIST,
                        onClick = { viewModel.onAction(OrganizerAction.OnSetViewMode(FileViewMode.LIST)) },
                    )
                    RadioButtonItem(
                        "Grid",
                        selected = state.viewMode == FileViewMode.GRID,
                        onClick = { viewModel.onAction(OrganizerAction.OnSetViewMode(FileViewMode.GRID)) },
                    )
                }
            }

            App()
        }
    }
}
