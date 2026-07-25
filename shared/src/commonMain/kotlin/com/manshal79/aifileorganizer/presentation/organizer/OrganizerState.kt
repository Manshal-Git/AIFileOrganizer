package com.manshal79.aifileorganizer.presentation.organizer

import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.model.TokenUsage
import com.manshal79.aifileorganizer.presentation.UiText

data class OrganizerState(
    val folderPath: String? = null,
    val mode: OrganizeMode = OrganizeMode.REVIEW,
    val viewMode: FileViewMode = FileViewMode.LIST,
    val files: List<FileItemUi> = emptyList(),
    val collapsedTypes: Set<FileType> = emptySet(),
    val renameHistory: List<RenameRecord> = emptyList(),
    val filesToProcess: Int = 0,
    val tokenUsage: TokenUsage = TokenUsage.Zero,
    val llmRequestCount: Int = 0,
    val isScanning: Boolean = false,
    val error: UiText? = null,
) {
    val hasApplicableSuggestions: Boolean
        get() = files.any { it.status is FileItemStatus.Suggested || it.status is FileItemStatus.RenameFailed }

    val canUndo: Boolean
        get() = renameHistory.isNotEmpty()

    val suggestionsReadyCount: Int
        get() = files.count { it.status is FileItemStatus.Suggested || it.status is FileItemStatus.RenameFailed }

    val renamedCount: Int
        get() = files.count { it.status is FileItemStatus.Renamed }

    /** Analyzable files the AI has finished with, whether it produced a name or failed. */
    val analyzedCount: Int
        get() = files.count {
            when (it.status) {
                FileItemStatus.Pending, FileItemStatus.Suggesting -> false
                else -> true
            }
        }

    /** True while any long-running work is in flight — used to gate session-wide actions. */
    val isBusy: Boolean
        get() = isScanning || files.any {
            it.status == FileItemStatus.Suggesting || it.status == FileItemStatus.Renaming
        }

    /** Last path segment of the selected folder, for the side nav header. */
    val folderName: String?
        get() = folderPath?.trimEnd('/', '\\')?.substringAfterLast('/')?.substringAfterLast('\\')
            ?.takeIf { it.isNotBlank() }
}

enum class OrganizeMode {
    REVIEW,
    AUTO_PILOT,
}

enum class FileViewMode {
    LIST,
    GRID,
}
