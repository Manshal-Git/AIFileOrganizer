package com.manshal79.aifileorganizer.data.filesystem

import kotlin.test.Test
import kotlin.test.assertEquals

class FileNameSanitizerTest {

    @Test
    fun keepsAlreadyCleanSnakeCaseName() {
        assertEquals("q3_sales_report", FileNameSanitizer.sanitizeBaseName("q3_sales_report"))
    }

    @Test
    fun replacesPathSeparatorsAndIllegalChars() {
        assertEquals("a_b_c", FileNameSanitizer.sanitizeBaseName("a/b*c"))
        assertEquals("report_final", FileNameSanitizer.sanitizeBaseName("report.final"))
    }

    @Test
    fun collapsesWhitespaceAndUnderscoreRuns() {
        assertEquals("hello_world", FileNameSanitizer.sanitizeBaseName("  hello   world  "))
        assertEquals("a_b", FileNameSanitizer.sanitizeBaseName("a___b"))
    }

    @Test
    fun trimsLeadingAndTrailingSeparators() {
        assertEquals("name", FileNameSanitizer.sanitizeBaseName("__name__"))
    }

    @Test
    fun fallsBackWhenEverythingIsStripped() {
        assertEquals("untitled", FileNameSanitizer.sanitizeBaseName("..."))
        assertEquals("untitled", FileNameSanitizer.sanitizeBaseName("   "))
    }

    @Test
    fun capsLength() {
        val result = FileNameSanitizer.sanitizeBaseName("a".repeat(200))
        assertEquals(100, result.length)
    }
}
