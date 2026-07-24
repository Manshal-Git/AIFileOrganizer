package com.manshal79.aifileorganizer.domain.model

data class ScannedFile(
    val path: String,
    val name: String,
    val extension: String,
    val sizeBytes: Long,
    val type: FileType,
)

enum class FileType {
    CODE,
    PDF,
    IMAGE,
    TEXT_DOCUMENT,
    UNSUPPORTED,
}
