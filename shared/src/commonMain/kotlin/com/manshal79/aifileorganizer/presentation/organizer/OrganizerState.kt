package com.manshal79.aifileorganizer.presentation.organizer

import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.presentation.UiText

data class OrganizerState(
    val folderPath: String? = null,
    val mode: OrganizeMode = OrganizeMode.REVIEW,
    val viewMode: FileViewMode = FileViewMode.LIST,
    val files: List<FileItemUi> = emptyList(),
    val collapsedTypes: Set<FileType> = emptySet(),
    val renameHistory: List<RenameRecord> = emptyList(),
    val isScanning: Boolean = false,
    val error: UiText? = null,
) {
    val hasApplicableSuggestions: Boolean
        get() = files.any { it.status is FileItemStatus.Suggested || it.status is FileItemStatus.RenameFailed }

    val canUndo: Boolean
        get() = renameHistory.isNotEmpty()
}

enum class OrganizeMode {
    REVIEW,
    AUTO_PILOT,
}

enum class FileViewMode {
    LIST,
    GRID,
}
