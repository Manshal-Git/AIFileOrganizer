package com.manshal79.aifileorganizer.presentation.organizer

import androidx.compose.ui.graphics.ImageBitmap
import com.manshal79.aifileorganizer.domain.model.FileType

sealed interface FilePreview {
    data class Image(val bitmap: ImageBitmap) : FilePreview
    data class Text(val snippet: String) : FilePreview
    data object Unavailable : FilePreview
}

/** Best-effort preview for a grid/list item. Never throws — falls back to [FilePreview.Unavailable]. */
expect suspend fun loadFilePreview(path: String, type: FileType): FilePreview
