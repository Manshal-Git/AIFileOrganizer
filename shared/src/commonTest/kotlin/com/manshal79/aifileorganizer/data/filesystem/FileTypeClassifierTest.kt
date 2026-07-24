package com.manshal79.aifileorganizer.data.filesystem

import com.manshal79.aifileorganizer.domain.model.FileType
import kotlin.test.Test
import kotlin.test.assertEquals

class FileTypeClassifierTest {

    @Test
    fun classifiesPdfExtension() {
        assertEquals(FileType.PDF, FileTypeClassifier.classify("pdf"))
        assertEquals(FileType.PDF, FileTypeClassifier.classify("PDF"))
    }

    @Test
    fun classifiesImageExtensions() {
        assertEquals(FileType.IMAGE, FileTypeClassifier.classify("jpg"))
        assertEquals(FileType.IMAGE, FileTypeClassifier.classify("png"))
    }

    @Test
    fun classifiesTextDocumentExtensions() {
        assertEquals(FileType.TEXT_DOCUMENT, FileTypeClassifier.classify("txt"))
        assertEquals(FileType.TEXT_DOCUMENT, FileTypeClassifier.classify("docx"))
    }

    @Test
    fun classifiesUnknownExtensionAsUnsupported() {
        assertEquals(FileType.UNSUPPORTED, FileTypeClassifier.classify("exe"))
    }
}
