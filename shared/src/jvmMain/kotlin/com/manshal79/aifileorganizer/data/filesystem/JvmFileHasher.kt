package com.manshal79.aifileorganizer.data.filesystem

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class JvmFileHasher : FileHasher {

    override suspend fun hash(path: String): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        File(path).inputStream().use { stream ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = stream.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val BUFFER_SIZE = 8192
    }
}
