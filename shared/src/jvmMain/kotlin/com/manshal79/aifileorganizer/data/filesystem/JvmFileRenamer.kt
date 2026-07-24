package com.manshal79.aifileorganizer.data.filesystem

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files

class JvmFileRenamer : FileRenamer {

    override suspend fun rename(sourcePath: String, desiredBaseName: String): RenameResult =
        withContext(Dispatchers.IO) {
            val source = File(sourcePath)
            require(source.exists()) { "File no longer exists: $sourcePath" }

            val directory = source.parentFile ?: error("File has no parent directory: $sourcePath")
            val extension = source.extension // empty string if none, without the dot
            val safeBase = FileNameSanitizer.sanitizeBaseName(desiredBaseName)

            var target = File(directory, buildFileName(safeBase, extension))

            // Already correctly named — nothing to do.
            if (target.absolutePath == source.absolutePath) {
                return@withContext RenameResult(source.absolutePath, source.name, unchanged = true)
            }

            // Resolve collisions, but treat "collides with the source itself" (case-only rename on a
            // case-insensitive filesystem) as free rather than appending a suffix.
            var counter = 1
            while (target.exists() && !Files.isSameFile(source.toPath(), target.toPath())) {
                target = File(directory, buildFileName("${safeBase}_$counter", extension))
                counter++
            }

            Files.move(source.toPath(), target.toPath())
            RenameResult(target.absolutePath, target.name, unchanged = false)
        }

    override suspend fun moveTo(sourcePath: String, targetPath: String) = withContext(Dispatchers.IO) {
        val source = File(sourcePath)
        val target = File(targetPath)
        require(source.exists()) { "File no longer exists: $sourcePath" }
        require(!target.exists() || Files.isSameFile(source.toPath(), target.toPath())) {
            "Cannot restore: a file already exists at $targetPath"
        }
        Files.move(source.toPath(), target.toPath())
        Unit
    }

    private fun buildFileName(base: String, extension: String): String =
        if (extension.isEmpty()) base else "$base.$extension"
}
