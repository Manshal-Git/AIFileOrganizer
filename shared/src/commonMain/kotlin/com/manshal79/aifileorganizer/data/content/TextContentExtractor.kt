package com.manshal79.aifileorganizer.data.content

interface TextContentExtractor {
    suspend fun extract(path: String): String
}
