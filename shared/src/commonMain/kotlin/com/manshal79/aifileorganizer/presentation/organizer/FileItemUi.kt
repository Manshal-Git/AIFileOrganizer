package com.manshal79.aifileorganizer.presentation.organizer

import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.model.ScannedFile

data class FileItemUi(
    val id: String,
    val name: String,
    val path: String,
    val type: FileType,
)

fun ScannedFile.toFileItemUi(): FileItemUi = FileItemUi(
    id = path,
    name = name,
    path = path,
    type = type,
)
