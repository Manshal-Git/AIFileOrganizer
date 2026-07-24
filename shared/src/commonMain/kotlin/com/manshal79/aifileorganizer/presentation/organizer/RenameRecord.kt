package com.manshal79.aifileorganizer.presentation.organizer

/** One applied rename, kept in memory so it can be undone within the session. */
data class RenameRecord(
    val fileId: String,
    val originalPath: String,
    val newPath: String,
    /** The suggestion to restore on the file item when this rename is undone. */
    val suggested: FileItemStatus.Suggested,
)
