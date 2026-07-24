package com.manshal79.aifileorganizer.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manshal79.aifileorganizer.data.content.TextContentExtractor
import com.manshal79.aifileorganizer.data.filesystem.FileScanner
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.usecase.RenameSuggestionUseCase
import com.manshal79.aifileorganizer.presentation.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrganizerViewModel(
    private val fileScanner: FileScanner,
    private val textContentExtractor: TextContentExtractor,
    private val renameSuggestionUseCase: RenameSuggestionUseCase,
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
                suggestNamesForTextLikeFiles()
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

    // PDF and image extraction aren't built yet, so only plain-text-readable
    // files (text documents and source/markup files) get a suggestion for now.
    private suspend fun suggestNamesForTextLikeFiles() {
        val textLikeFiles = _state.value.files.filter {
            it.type == FileType.TEXT_DOCUMENT || it.type == FileType.CODE
        }
        for (file in textLikeFiles) {
            updateFile(file.id) { it.copy(status = FileItemStatus.Suggesting) }
            try {
                val content = textContentExtractor.extract(file.path)
                val suggestion = renameSuggestionUseCase.suggest(fileName = file.name, content = content)
                updateFile(file.id) {
                    it.copy(status = FileItemStatus.Suggested(suggestion.suggestedName, suggestion.category))
                }
            } catch (e: Exception) {
                updateFile(file.id) {
                    it.copy(status = FileItemStatus.Failed(e.message ?: "Couldn't suggest a name"))
                }
            }
        }
    }

    private fun updateFile(id: String, transform: (FileItemUi) -> FileItemUi) {
        _state.update { state ->
            state.copy(files = state.files.map { if (it.id == id) transform(it) else it })
        }
    }
}
