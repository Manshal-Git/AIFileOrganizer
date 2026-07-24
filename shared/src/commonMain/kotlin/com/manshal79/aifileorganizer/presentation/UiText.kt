package com.manshal79.aifileorganizer.presentation

/**
 * No string-resource variant yet: this project has no localized string bundle.
 * Add one here if/when resource-backed strings are introduced.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
}

fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
}
