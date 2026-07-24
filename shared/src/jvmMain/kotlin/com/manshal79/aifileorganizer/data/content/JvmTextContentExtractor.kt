package com.manshal79.aifileorganizer.data.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class JvmTextContentExtractor : TextContentExtractor {

    override suspend fun extract(path: String): String = withContext(Dispatchers.IO) {
        File(path).readText().take(MAX_CONTENT_CHARS)
    }

    private companion object {
        // Keeps the prompt well within the configured model's context window.
        const val MAX_CONTENT_CHARS = 8_000
    }
}
