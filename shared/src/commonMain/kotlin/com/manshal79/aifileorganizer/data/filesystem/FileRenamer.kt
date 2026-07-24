package com.manshal79.aifileorganizer.data.filesystem

data class RenameResult(
    val newPath: String,
    val newName: String,
    /** True when the file already had the desired name and nothing was moved. */
    val unchanged: Boolean,
)

interface FileRenamer {
    /**
     * Renames the file at [sourcePath] to [desiredBaseName] (without extension). The original
     * extension is preserved, the name is sanitized, and collisions are resolved with a numeric
     * suffix. Throws if the source no longer exists or the move fails.
     */
    suspend fun rename(sourcePath: String, desiredBaseName: String): RenameResult

    /** Exact move used to undo a previous rename. Throws if [targetPath] is already taken. */
    suspend fun moveTo(sourcePath: String, targetPath: String)
}
