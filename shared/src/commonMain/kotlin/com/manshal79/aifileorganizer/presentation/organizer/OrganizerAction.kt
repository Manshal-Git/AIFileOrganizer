package com.manshal79.aifileorganizer.presentation.organizer

sealed interface OrganizerAction {
    data class OnFolderSelected(val path: String) : OrganizerAction
    data object OnToggleMode : OrganizerAction
    data object OnRescanClick : OrganizerAction
}
