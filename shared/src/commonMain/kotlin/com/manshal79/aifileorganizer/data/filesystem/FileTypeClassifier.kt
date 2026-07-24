package com.manshal79.aifileorganizer.data.filesystem

import com.manshal79.aifileorganizer.domain.model.FileType

object FileTypeClassifier {

    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    private val textDocumentExtensions = setOf("txt", "md", "docx")
    private val codeDocumentExtensions = setOf("html", "htm", "js", "kt", "java")

    fun classify(extension: String): FileType {
        return when (extension.lowercase()) {
            "pdf" -> FileType.PDF
            in imageExtensions -> FileType.IMAGE
            in textDocumentExtensions -> FileType.TEXT_DOCUMENT
            in codeDocumentExtensions -> FileType.CODE
            else -> FileType.UNSUPPORTED
        }
    }
}
