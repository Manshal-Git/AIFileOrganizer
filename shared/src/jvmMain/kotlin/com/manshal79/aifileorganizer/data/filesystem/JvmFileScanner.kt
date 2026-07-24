package com.manshal79.aifileorganizer.data.filesystem

import com.manshal79.aifileorganizer.domain.model.ScannedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class JvmFileScanner : FileScanner {

    override suspend fun scan(rootPath: String): List<ScannedFile> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.isDirectory) return@withContext emptyList()

        root.walkTopDown()
            .onEnter { directory -> !directory.name.startsWith(".") }
            .filter { it.isFile && !it.name.startsWith(".") }
            .map { file ->
                val extension = file.extension
                ScannedFile(
                    path = file.absolutePath,
                    name = file.name,
                    extension = extension,
                    sizeBytes = file.length(),
                    type = FileTypeClassifier.classify(extension),
                )
            }
            .toList()
    }
}
