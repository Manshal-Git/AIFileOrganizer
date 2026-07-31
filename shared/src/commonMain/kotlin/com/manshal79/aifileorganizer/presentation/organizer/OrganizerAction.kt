package com.manshal79.aifileorganizer.presentation.organizer

import com.manshal79.aifileorganizer.domain.model.FileType

sealed interface OrganizerAction {
    data class OnFolderSelected(val path: String) : OrganizerAction
    data object OnToggleMode : OrganizerAction
    data object OnRescanClick : OrganizerAction
    data object OnToggleViewMode : OrganizerAction
    data class OnSetViewMode(val viewMode: FileViewMode) : OrganizerAction
    data class OnToggleGroupCollapsed(val type: FileType) : OrganizerAction
    data class OnApplyRename(val fileId: String) : OrganizerAction
    data object OnApplyAll : OrganizerAction
    data object OnUndoAll : OrganizerAction
    data object OnDismissError : OrganizerAction
    data object OnToggleThinking : OrganizerAction
    data object OnRefreshModels : OrganizerAction
    data class OnSelectModel(val modelId: String) : OrganizerAction
}
