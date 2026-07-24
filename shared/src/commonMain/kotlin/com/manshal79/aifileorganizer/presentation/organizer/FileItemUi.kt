package com.manshal79.aifileorganizer.presentation.organizer

import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.model.ScannedFile

data class FileItemUi(
    val id: String,
    val name: String,
    val path: String,
    val type: FileType,
    val status: FileItemStatus = FileItemStatus.Pending,
)

sealed interface FileItemStatus {
    data object Pending : FileItemStatus
    data object Suggesting : FileItemStatus
    data class Suggested(val suggestedName: String, val category: String) : FileItemStatus
    data object Renaming : FileItemStatus
    data class Renamed(val newName: String) : FileItemStatus
    /** A rename attempt failed; the suggestion is kept so it can be retried. */
    data class RenameFailed(
        val suggestedName: String,
        val category: String,
        val message: String,
    ) : FileItemStatus
    /** Content extraction or the suggestion request failed. */
    data class Failed(val message: String) : FileItemStatus
}

fun ScannedFile.toFileItemUi(): FileItemUi = FileItemUi(
    id = path,
    name = name,
    path = path,
    type = type,
)
