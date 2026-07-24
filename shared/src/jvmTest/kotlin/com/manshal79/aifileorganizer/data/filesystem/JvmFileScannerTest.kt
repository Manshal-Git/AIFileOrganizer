package com.manshal79.aifileorganizer.data.filesystem

import com.manshal79.aifileorganizer.domain.model.FileType
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JvmFileScannerTest {

    @Test
    fun scanClassifiesFilesAndSkipsHidden() = runBlocking {
        val tempDir = Files.createTempDirectory("file-scanner-test").toFile()
        try {
            tempDir.resolve("report.pdf").writeText("pdf content")
            tempDir.resolve("photo.jpg").writeText("image bytes")
            tempDir.resolve("notes.txt").writeText("plain text")
            tempDir.resolve("archive.zip").writeText("zip content")
            tempDir.resolve(".hidden").writeText("should be skipped")

            val result = JvmFileScanner().scan(tempDir.absolutePath)

            assertEquals(4, result.size)
            assertEquals(FileType.PDF, result.single { it.name == "report.pdf" }.type)
            assertEquals(FileType.IMAGE, result.single { it.name == "photo.jpg" }.type)
            assertEquals(FileType.TEXT_DOCUMENT, result.single { it.name == "notes.txt" }.type)
            assertEquals(FileType.UNSUPPORTED, result.single { it.name == "archive.zip" }.type)
            assertTrue(result.none { it.name == ".hidden" })
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
