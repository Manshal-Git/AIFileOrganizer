package com.manshal79.aifileorganizer.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manshal79.aifileorganizer.data.filesystem.FileScanner
import com.manshal79.aifileorganizer.presentation.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrganizerViewModel(
    private val fileScanner: FileScanner,
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizerState())
    val state = _state.asStateFlow()

    fun onAction(action: OrganizerAction) {
        when (action) {
            is OrganizerAction.OnFolderSelected -> selectFolder(action.path)
            OrganizerAction.OnToggleMode -> toggleMode()
            OrganizerAction.OnRescanClick -> rescan()
        }
    }

    private fun selectFolder(path: String) {
        _state.update { it.copy(folderPath = path) }
        scan(path)
    }

    private fun rescan() {
        val path = _state.value.folderPath ?: return
        scan(path)
    }

    private fun toggleMode() {
        _state.update {
            val nextMode = if (it.mode == OrganizeMode.REVIEW) OrganizeMode.AUTO_PILOT else OrganizeMode.REVIEW
            it.copy(mode = nextMode)
        }
    }

    private fun scan(path: String) {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, error = null) }
            try {
                val scanned = fileScanner.scan(path)
                _state.update {
                    it.copy(
                        files = scanned.map { file -> file.toFileItemUi() },
                        isScanning = false,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isScanning = false,
                        error = UiText.DynamicString("Couldn't scan folder: ${e.message}"),
                    )
                }
            }
        }
    }
}
