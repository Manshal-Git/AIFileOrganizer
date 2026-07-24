package com.manshal79.aifileorganizer.data.filesystem

import com.manshal79.aifileorganizer.domain.model.ScannedFile

interface FileScanner {
    suspend fun scan(rootPath: String): List<ScannedFile>
}
