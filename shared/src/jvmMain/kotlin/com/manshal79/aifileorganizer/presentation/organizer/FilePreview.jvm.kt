package com.manshal79.aifileorganizer.presentation.organizer

import androidx.compose.ui.graphics.decodeToImageBitmap
import com.manshal79.aifileorganizer.domain.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual suspend fun loadFilePreview(path: String, type: FileType): FilePreview = withContext(Dispatchers.IO) {
    when (type) {
        FileType.IMAGE -> runCatching {
            FilePreview.Image(File(path).readBytes().decodeToImageBitmap())
        }.getOrDefault(FilePreview.Unavailable)

        FileType.TEXT_DOCUMENT, FileType.CODE -> runCatching {
            FilePreview.Text(File(path).readText().take(SNIPPET_CHARS))
        }.getOrDefault(FilePreview.Unavailable)

        FileType.PDF, FileType.UNSUPPORTED -> FilePreview.Unavailable
    }
}

private const val SNIPPET_CHARS = 160
