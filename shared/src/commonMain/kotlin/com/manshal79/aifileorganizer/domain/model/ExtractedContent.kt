package com.manshal79.aifileorganizer.domain.model

sealed interface ExtractedContent {
    data class Text(val text: String) : ExtractedContent
    data class Image(val path: String) : ExtractedContent
}
