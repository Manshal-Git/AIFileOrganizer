package com.manshal79.aifileorganizer.presentation.organizer

import com.manshal79.aifileorganizer.presentation.UiText

data class OrganizerState(
    val folderPath: String? = null,
    val mode: OrganizeMode = OrganizeMode.REVIEW,
    val files: List<FileItemUi> = emptyList(),
    val isScanning: Boolean = false,
    val error: UiText? = null,
)

enum class OrganizeMode {
    REVIEW,
    AUTO_PILOT,
}
