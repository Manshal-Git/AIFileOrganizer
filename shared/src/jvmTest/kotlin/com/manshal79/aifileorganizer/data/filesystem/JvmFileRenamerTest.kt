package com.manshal79.aifileorganizer.data.filesystem

import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JvmFileRenamerTest {

    private inline fun withTempDir(block: (java.io.File) -> Unit) {
        val dir = Files.createTempDirectory("file-renamer-test").toFile()
        try {
            block(dir)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun renamePreservesExtension() = runBlocking {
        withTempDir { dir ->
            val original = dir.resolve("foo.txt").apply { writeText("content") }

            val result = JvmFileRenamer().rename(original.absolutePath, "bar")

            assertEquals("bar.txt", result.newName)
            assertFalse(result.unchanged)
            assertFalse(original.exists())
            assertTrue(dir.resolve("bar.txt").exists())
        }
    }

    @Test
    fun renameResolvesCollisionWithSuffix() = runBlocking {
        withTempDir { dir ->
            dir.resolve("bar.txt").writeText("existing")
            val original = dir.resolve("foo.txt").apply { writeText("content") }

            val result = JvmFileRenamer().rename(original.absolutePath, "bar")

            assertEquals("bar_1.txt", result.newName)
            assertTrue(dir.resolve("bar.txt").exists())
            assertTrue(dir.resolve("bar_1.txt").exists())
        }
    }

    @Test
    fun renameIsNoOpWhenNameAlreadyCorrect() = runBlocking {
        withTempDir { dir ->
            val original = dir.resolve("report.txt").apply { writeText("content") }

            val result = JvmFileRenamer().rename(original.absolutePath, "report")

            assertTrue(result.unchanged)
            assertEquals("report.txt", result.newName)
            assertTrue(original.exists())
        }
    }

    @Test
    fun renameSanitizesIllegalCharacters() = runBlocking {
        withTempDir { dir ->
            val original = dir.resolve("foo.txt").apply { writeText("content") }

            val result = JvmFileRenamer().rename(original.absolutePath, "a/b*c")

            assertEquals("a_b_c.txt", result.newName)
        }
    }

    @Test
    fun renameHandlesFileWithoutExtension() = runBlocking {
        withTempDir { dir ->
            val original = dir.resolve("LICENSE").apply { writeText("content") }

            val result = JvmFileRenamer().rename(original.absolutePath, "license_mit")

            assertEquals("license_mit", result.newName)
            assertTrue(dir.resolve("license_mit").exists())
        }
    }

    @Test
    fun moveToRestoresOriginalName() = runBlocking {
        withTempDir { dir ->
            val original = dir.resolve("foo.txt").apply { writeText("content") }
            val renamer = JvmFileRenamer()

            val result = renamer.rename(original.absolutePath, "bar")
            renamer.moveTo(result.newPath, original.absolutePath)

            assertTrue(original.exists())
            assertFalse(dir.resolve("bar.txt").exists())
        }
    }
}
