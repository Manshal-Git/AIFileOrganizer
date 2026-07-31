package com.manshal79.aifileorganizer.data.filesystem

/** Content-addressable digest of a file, used to key the rename-suggestion cache. */
interface FileHasher {
    suspend fun hash(path: String): String
}
