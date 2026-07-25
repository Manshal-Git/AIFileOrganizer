package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.presentation.designsystem.AppTheme
import com.manshal79.aifileorganizer.presentation.organizer.OllamaModelUi
import kotlin.test.Test

@OptIn(ExperimentalComposeUiApi::class)
class ModelPickerDialogRenderTest {

    private fun renderDialog(
        models: List<OllamaModelUi>,
        selectedModelId: String? = null,
        isLoading: Boolean = false,
    ) {
        val scene = ImageComposeScene(width = 800, height = 800, density = Density(1f)) {
            AppTheme {
                ModelPickerDialog(
                    models = models,
                    selectedModelId = selectedModelId,
                    isLoading = isLoading,
                    onSelect = {},
                    onRefresh = {},
                    onDismiss = {},
                )
            }
        }
        try {
            scene.render()
        } finally {
            scene.close()
        }
    }

    @Test
    fun `renders the empty state`() {
        renderDialog(models = emptyList())
    }

    @Test
    fun `renders the loading state`() {
        renderDialog(models = emptyList(), isLoading = true)
    }

    @Test
    fun `renders the populated list with the selected model expanded`() {
        renderDialog(models = sampleModels, selectedModelId = sampleModels.first().id)
    }

    @Test
    fun `renders a model without vision support`() {
        renderDialog(models = sampleModels, selectedModelId = sampleModels.last().id)
    }

    private companion object {
        val sampleModels = listOf(
            OllamaModelUi(
                id = "gemma3:4b",
                displayName = "gemma3:4b",
                family = "gemma3",
                parameterLabel = "4.3B params",
                sizeLabel = "2.9 GB",
                contextLengthLabel = "40,960 tokens",
                quantizationLevel = "Q4_K_M",
                supportsVision = true,
                supportsTools = true,
                supportedFileTypes = listOf(FileType.TEXT_DOCUMENT, FileType.CODE, FileType.IMAGE),
                isRecommended = true,
            ),
            OllamaModelUi(
                id = "llama3.2:1b",
                displayName = "llama3.2:1b",
                family = "llama",
                parameterLabel = "1.2B params",
                sizeLabel = "1.3 GB",
                contextLengthLabel = "8,192 tokens",
                quantizationLevel = "Q8_0",
                supportsVision = false,
                supportsTools = true,
                supportedFileTypes = listOf(FileType.TEXT_DOCUMENT, FileType.CODE),
                isRecommended = false,
            ),
        )
    }
}
