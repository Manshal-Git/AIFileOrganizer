package com.manshal79.aifileorganizer.presentation.organizer

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.model.TokenUsage
import com.manshal79.aifileorganizer.presentation.designsystem.AppTheme
import kotlin.test.Test

/**
 * Renders the screen off-screen in each of its states.
 *
 * This is a crash smoke test, not a visual one — it catches composition/layout failures
 * (bad icon path data, unbounded-height measurements, missing state handling) that a
 * compile check can't see. Visual review still happens by running the app.
 */
@OptIn(ExperimentalComposeUiApi::class)
class OrganizerScreenRenderTest {

    private fun renderScreen(state: OrganizerState) {
        val scene = ImageComposeScene(
            width = 1440,
            height = 900,
            density = Density(1f),
        ) {
            AppTheme {
                OrganizerScreen(state = state, onAction = {}, onPickFolderClick = {})
            }
        }
        try {
            scene.render()
        } finally {
            scene.close()
        }
    }

    @Test
    fun `renders the empty state with no folder selected`() {
        renderScreen(OrganizerState())
    }

    @Test
    fun `renders the scanning skeleton`() {
        renderScreen(OrganizerState(folderPath = "/tmp/inbox", isScanning = true))
    }

    @Test
    fun `renders a folder with no usable files`() {
        renderScreen(OrganizerState(folderPath = "/tmp/inbox"))
    }

    @Test
    fun `renders every file status in list mode`() {
        renderScreen(populatedState)
    }

    @Test
    fun `renders every file status in grid mode`() {
        renderScreen(populatedState.copy(viewMode = FileViewMode.GRID))
    }

    @Test
    fun `renders with a collapsed group`() {
        renderScreen(populatedState.copy(collapsedTypes = setOf(FileType.IMAGE)))
    }

    @Test
    fun `renders the error toast`() {
        renderScreen(
            populatedState.copy(
                error = com.manshal79.aifileorganizer.presentation.UiText.DynamicString(
                    "Couldn't scan folder: permission denied",
                ),
            ),
        )
    }

    @Test
    fun `renders the ollama unavailable banner`() {
        renderScreen(populatedState.copy(ollamaUnavailable = true))
    }

    private companion object {
        // One file per status, so a rendering failure in any branch surfaces here.
        val populatedState = OrganizerState(
            folderPath = "/Users/mansh/Documents/screenshots",
            filesToProcess = 6,
            tokenUsage = TokenUsage(inputTokens = 12_480, outputTokens = 342),
            llmRequestCount = 6,
            files = listOf(
                FileItemUi("1", "Screenshot_1.png", "/1", FileType.IMAGE, FileItemStatus.Pending),
                FileItemUi("2", "Screenshot_2.png", "/2", FileType.IMAGE, FileItemStatus.Suggesting),
                FileItemUi(
                    "3",
                    "Screenshot_3.png",
                    "/3",
                    FileType.IMAGE,
                    FileItemStatus.Suggested("jigsaw-puzzle-medium", "puzzles"),
                    tokenUsage = TokenUsage(inputTokens = 1_842, outputTokens = 27),
                ),
                FileItemUi("4", "notes.md", "/4", FileType.TEXT_DOCUMENT, FileItemStatus.Renaming),
                FileItemUi(
                    "5",
                    "old.md",
                    "/5",
                    FileType.TEXT_DOCUMENT,
                    FileItemStatus.Renamed("meeting-notes.md"),
                    tokenUsage = TokenUsage(inputTokens = 980, outputTokens = 22),
                ),
                FileItemUi(
                    "6",
                    "draft.md",
                    "/6",
                    FileType.TEXT_DOCUMENT,
                    FileItemStatus.RenameFailed("agenda", "notes", "Target already exists"),
                ),
                FileItemUi(
                    "7",
                    "locked.txt",
                    "/7",
                    FileType.TEXT_DOCUMENT,
                    FileItemStatus.Failed("Couldn't read file: permission denied"),
                ),
                FileItemUi("8", "Main.kt", "/8", FileType.CODE, FileItemStatus.Pending),
                FileItemUi("9", "invoice.pdf", "/9", FileType.PDF, FileItemStatus.Pending),
            ),
        )
    }
}
