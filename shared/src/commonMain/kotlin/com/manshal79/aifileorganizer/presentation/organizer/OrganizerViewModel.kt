package com.manshal79.aifileorganizer.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manshal79.aifileorganizer.data.content.TextContentExtractor
import com.manshal79.aifileorganizer.data.filesystem.FileRenamer
import com.manshal79.aifileorganizer.data.filesystem.FileScanner
import com.manshal79.aifileorganizer.domain.model.ExtractedContent
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
    private val fileRenamer: FileRenamer,
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizerState())
    val state = _state.asStateFlow()

    fun onAction(action: OrganizerAction) {
        when (action) {
            is OrganizerAction.OnFolderSelected -> selectFolder(action.path)
            OrganizerAction.OnToggleMode -> toggleMode()
            OrganizerAction.OnRescanClick -> rescan()
            OrganizerAction.OnToggleViewMode -> toggleViewMode()
            is OrganizerAction.OnToggleGroupCollapsed -> toggleGroupCollapsed(action.type)
            is OrganizerAction.OnApplyRename -> applyRename(action.fileId)
            OrganizerAction.OnApplyAll -> applyAll()
            OrganizerAction.OnUndoAll -> undoAll()
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

    private fun toggleViewMode() {
        _state.update {
            val nextViewMode = if (it.viewMode == FileViewMode.LIST) FileViewMode.GRID else FileViewMode.LIST
            it.copy(viewMode = nextViewMode)
        }
    }

    private fun toggleGroupCollapsed(type: FileType) {
        _state.update {
            val nextCollapsed = if (type in it.collapsedTypes) it.collapsedTypes - type else it.collapsedTypes + type
            it.copy(collapsedTypes = nextCollapsed)
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
                suggestNamesForSupportedFiles()
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

    // PDF extraction isn't built yet, so text/code files (read as plain text) and
    // images (handed to the model directly, no separate extraction step needed
    // since Koog reads the image bytes itself) are the only types suggested for now.
    private suspend fun suggestNamesForSupportedFiles() {
        val supportedFiles = _state.value.files.filter {
            it.type == FileType.TEXT_DOCUMENT || it.type == FileType.CODE || it.type == FileType.IMAGE
        }
        for (file in supportedFiles) {
            updateFile(file.id) { it.copy(status = FileItemStatus.Suggesting) }
            try {
                val content = when (file.type) {
                    FileType.IMAGE -> ExtractedContent.Image(file.path)
                    else -> ExtractedContent.Text(textContentExtractor.extract(file.path))
                }
                val suggestion = renameSuggestionUseCase.suggest(fileName = file.name, content = content)
                updateFile(file.id) {
                    it.copy(status = FileItemStatus.Suggested(suggestion.suggestedName, suggestion.category))
                }
                // In auto-pilot the suggestion is applied immediately; in review it waits for the user.
                if (_state.value.mode == OrganizeMode.AUTO_PILOT) {
                    applyRenameInternal(file.id)
                }
            } catch (e: Exception) {
                updateFile(file.id) {
                    it.copy(status = FileItemStatus.Failed(e.message ?: "Couldn't suggest a name"))
                }
            }
        }
    }

    private fun applyRename(fileId: String) {
        viewModelScope.launch { applyRenameInternal(fileId) }
    }

    private fun applyAll() {
        viewModelScope.launch {
            val ids = _state.value.files
                .filter { it.status is FileItemStatus.Suggested || it.status is FileItemStatus.RenameFailed }
                .map { it.id }
            for (id in ids) applyRenameInternal(id)
        }
    }

    private suspend fun applyRenameInternal(fileId: String) {
        val file = _state.value.files.find { it.id == fileId } ?: return
        val suggestion = file.status.asSuggestion() ?: return

        updateFile(fileId) { it.copy(status = FileItemStatus.Renaming) }
        try {
            val result = fileRenamer.rename(sourcePath = file.path, desiredBaseName = suggestion.suggestedName)
            updateFile(fileId) { it.copy(status = FileItemStatus.Renamed(result.newName)) }
            if (!result.unchanged) {
                val record = RenameRecord(
                    fileId = fileId,
                    originalPath = file.path,
                    newPath = result.newPath,
                    suggested = suggestion,
                )
                _state.update { it.copy(renameHistory = it.renameHistory + record) }
            }
        } catch (e: Exception) {
            updateFile(fileId) {
                it.copy(
                    status = FileItemStatus.RenameFailed(
                        suggestedName = suggestion.suggestedName,
                        category = suggestion.category,
                        message = e.message ?: "Rename failed",
                    ),
                )
            }
        }
    }

    private fun undoAll() {
        viewModelScope.launch {
            // Reverse order so files renamed later are moved back first.
            for (record in _state.value.renameHistory.reversed()) {
                try {
                    fileRenamer.moveTo(sourcePath = record.newPath, targetPath = record.originalPath)
                    updateFile(record.fileId) { it.copy(status = record.suggested) }
                } catch (e: Exception) {
                    updateFile(record.fileId) {
                        it.copy(
                            status = FileItemStatus.RenameFailed(
                                suggestedName = record.suggested.suggestedName,
                                category = record.suggested.category,
                                message = "Undo failed: ${e.message}",
                            ),
                        )
                    }
                }
            }
            _state.update { it.copy(renameHistory = emptyList()) }
        }
    }

    private fun FileItemStatus.asSuggestion(): FileItemStatus.Suggested? = when (this) {
        is FileItemStatus.Suggested -> this
        is FileItemStatus.RenameFailed -> FileItemStatus.Suggested(suggestedName, category)
        else -> null
    }

    private fun updateFile(id: String, transform: (FileItemUi) -> FileItemUi) {
        _state.update { state ->
            state.copy(files = state.files.map { if (it.id == id) transform(it) else it })
        }
    }
}
