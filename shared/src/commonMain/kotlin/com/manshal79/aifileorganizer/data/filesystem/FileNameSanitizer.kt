package com.manshal79.aifileorganizer.data.filesystem

/**
 * Turns a raw, model-suggested base name (no extension) into a filesystem-safe one.
 * The LLM is asked for snake_case with no special characters, but this is defense in depth:
 * a bad suggestion must never produce an unsafe path or an empty name.
 */
object FileNameSanitizer {

    private const val MAX_LENGTH = 100
    private const val FALLBACK = "untitled"

    // Path separators, Windows-reserved chars, dots (avoid hidden files / double extensions),
    // and ASCII control characters — all collapsed to an underscore.
    private val illegalChars = Regex("""[\\/:*?"<>|.\x00-\x1F]""")
    private val whitespace = Regex("""\s+""")
    private val underscoreRuns = Regex("""_+""")

    fun sanitizeBaseName(raw: String): String {
        var name = raw.trim()
        name = illegalChars.replace(name, "_")
        name = whitespace.replace(name, "_")
        name = underscoreRuns.replace(name, "_")
        name = name.trim('_', ' ')
        if (name.length > MAX_LENGTH) {
            name = name.substring(0, MAX_LENGTH).trim('_', ' ')
        }
        return name.ifBlank { FALLBACK }
    }
}
