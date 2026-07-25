package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.ui.graphics.vector.ImageVector
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons

internal val FileType.icon: ImageVector
    get() = when (this) {
        FileType.CODE -> AppIcons.Code
        FileType.PDF -> AppIcons.PictureAsPdf
        FileType.IMAGE -> AppIcons.Image
        FileType.TEXT_DOCUMENT -> AppIcons.Description
        FileType.UNSUPPORTED -> AppIcons.InsertDriveFile
    }

internal val FileType.label: String
    get() = when (this) {
        FileType.CODE -> "Code"
        FileType.PDF -> "PDF"
        FileType.IMAGE -> "Images"
        FileType.TEXT_DOCUMENT -> "Documents"
        FileType.UNSUPPORTED -> "Other"
    }

/** Announced by the row's icon tile so the file's kind isn't conveyed by the glyph alone. */
internal val FileType.accessibilityLabel: String
    get() = when (this) {
        FileType.CODE -> "Code file"
        FileType.PDF -> "PDF file"
        FileType.IMAGE -> "Image file"
        FileType.TEXT_DOCUMENT -> "Text document"
        FileType.UNSUPPORTED -> "File"
    }
