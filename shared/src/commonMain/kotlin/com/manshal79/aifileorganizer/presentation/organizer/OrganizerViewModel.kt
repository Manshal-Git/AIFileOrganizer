package com.manshal79.aifileorganizer.presentation.organizer

import ai.koog.prompt.executor.ollama.client.OllamaModelCard
import ai.koog.prompt.executor.ollama.client.toLLModel
import ai.koog.prompt.llm.LLModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manshal79.aifileorganizer.data.content.TextContentExtractor
import com.manshal79.aifileorganizer.data.filesystem.FileHasher
import com.manshal79.aifileorganizer.data.filesystem.FileRenamer
import com.manshal79.aifileorganizer.data.filesystem.FileScanner
import com.manshal79.aifileorganizer.domain.model.ExtractedContent
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.model.ModelOutputException
import com.manshal79.aifileorganizer.domain.model.OllamaModelInfo
import com.manshal79.aifileorganizer.domain.model.RenameSuggestionResult
import com.manshal79.aifileorganizer.domain.model.TokenUsage
import com.manshal79.aifileorganizer.domain.usecase.GetAvailableOllamaModelsUseCase
import com.manshal79.aifileorganizer.domain.usecase.RenameSuggestionUseCase
import com.manshal79.aifileorganizer.presentation.UiText
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class OrganizerViewModel(
    private val fileScanner: FileScanner,
    private val textContentExtractor: TextContentExtractor,
    private val renameSuggestionUseCase: RenameSuggestionUseCase,
    private val fileRenamer: FileRenamer,
    private val getAvailableOllamaModelsUseCase: GetAvailableOllamaModelsUseCase,
    private val fileHasher: FileHasher,
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizerState())
    val state = _state.asStateFlow()

    // Kept alongside state.availableModels (the UI-safe projection) so a real LLModel with its
    // actual Koog capabilities can be rebuilt for the use case without leaking that type into state.
    private var availableModelCards: List<OllamaModelCard> = emptyList()

    // Keyed by "$modelId:$thinking:$contentHash" so a rescan (or a duplicate file elsewhere) with
    // unchanged content skips the LLM entirely — the biggest lever on local inference cost.
    // Thinking is part of the key because it changes the answer. Session-scoped,
    // same as the rest of this app's state (no persistence layer yet).
    private val suggestionCache = mutableMapOf<String, RenameSuggestionResult>()

    init {
        viewModelScope.launch { loadModels() }
    }

    fun onAction(action: OrganizerAction) {
        when (action) {
            is OrganizerAction.OnFolderSelected -> selectFolder(action.path)
            OrganizerAction.OnToggleMode -> toggleMode()
            OrganizerAction.OnRescanClick -> rescan()
            OrganizerAction.OnToggleViewMode -> toggleViewMode()
            is OrganizerAction.OnSetViewMode -> setViewMode(action.viewMode)
            is OrganizerAction.OnToggleGroupCollapsed -> toggleGroupCollapsed(action.type)
            is OrganizerAction.OnApplyRename -> applyRename(action.fileId)
            OrganizerAction.OnApplyAll -> applyAll()
            OrganizerAction.OnUndoAll -> undoAll()
            OrganizerAction.OnDismissError -> _state.update {
                it.copy(error = null, ollamaUnavailable = false, modelSelectionRequired = false)
            }
            OrganizerAction.OnToggleThinking -> toggleThinking()
            OrganizerAction.OnRefreshModels -> viewModelScope.launch { loadModels() }
            is OrganizerAction.OnSelectModel -> selectModel(action.modelId)
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

    /** Applies to the next suggestion run; files already named keep the answer they got. */
    private fun toggleThinking() {
        _state.update { it.copy(thinkingEnabled = !it.thinkingEnabled) }
    }

    private fun setViewMode(viewMode: FileViewMode) {
        _state.update { it.copy(viewMode = viewMode) }
    }

    private fun toggleGroupCollapsed(type: FileType) {
        _state.update {
            val nextCollapsed = if (type in it.collapsedTypes) it.collapsedTypes - type else it.collapsedTypes + type
            it.copy(collapsedTypes = nextCollapsed)
        }
    }

    private fun scan(path: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    error = null,
                    ollamaUnavailable = false,
                    filesToProcess = 0
                )
            }
            try {
                val scanned = fileScanner.scan(path)
                val fileItems = scanned.map { file -> file.toFileItemUi() }
                _state.update { it.copy(files = fileItems, isScanning = false) }

                val ollamaAvailable = loadModels()
                if (fileItems.any { it.isSupportedType() } && !ollamaAvailable) {
                    _state.update { it.copy(ollamaUnavailable = true) }
                } else {
                    suggestNamesForSupportedFiles()
                }
            } catch (e: Exception) {
                logger.error(e) { "Folder scan failed (path='$path')" }
                _state.update {
                    it.copy(
                        isScanning = false,
                        error = UiText.DynamicString("Couldn't scan folder: ${e.message}"),
                    )
                }
            }
        }
    }

    /**
     * Refreshes the pickable model list. Returns whether Ollama actually responded.
     *
     * A failed fetch (Ollama momentarily down) leaves the last known-good list and selection
     * alone rather than wiping the picker — only a successful fetch replaces them. No model is
     * ever auto-picked: a heavy model loaded onto the user's machine without them choosing it
     * would be a bad surprise, so the user always selects one explicitly via the model picker.
     */
    private suspend fun loadModels(): Boolean {
        _state.update { it.copy(isLoadingModels = true) }
        val result = getAvailableOllamaModelsUseCase.getModels()
        result.onFailure { e ->
            // Expected whenever Ollama isn't running; the banner is the user-facing half of this.
            logger.warn(e) { "Couldn't list Ollama models — keeping the last known-good list" }
        }
        result.onSuccess { models ->
            availableModelCards = models.map(OllamaModelInfo::card)
            val uiModels = models.map { it.toOllamaModelUi() }
            _state.update { current ->
                // Clears the selection if the previously picked model was uninstalled;
                // never falls back to auto-picking a different one.
                val stillInstalled = uiModels.any { it.id == current.selectedModelId }
                current.copy(
                    availableModels = uiModels,
                    selectedModelId = current.selectedModelId.takeIf { stillInstalled },
                )
            }
        }
        _state.update { it.copy(isLoadingModels = false) }
        return result.isSuccess
    }

    /**
     * User picked a model from the header chip / picker dialog. If a scan was waiting on this
     * choice (files sat Pending because nothing was auto-selected), resume it now.
     */
    private fun selectModel(modelId: String) {
        val shouldResumeSuggestions = _state.value.modelSelectionRequired
        _state.update { it.copy(selectedModelId = modelId, modelSelectionRequired = false) }
        if (shouldResumeSuggestions) {
            viewModelScope.launch { suggestNamesForSupportedFiles() }
        }
    }

    private fun currentLLModel(): LLModel? =
        availableModelCards.find { it.name == _state.value.selectedModelId }?.toLLModel()

    // PDF extraction isn't built yet, so text/code files (read as plain text) and
    // images (handed to the model directly, no separate extraction step needed
    // since Koog reads the image bytes itself) are the only types suggested for now.
    private suspend fun suggestNamesForSupportedFiles() {
        val model = currentLLModel()
        if (model == null) {
            _state.update { it.copy(modelSelectionRequired = true) }
            return
        }

        // Fixed for the whole run so a mid-run toggle can't split one batch across two settings.
        val thinking = _state.value.thinkingRequest
        val supportedFiles = _state.value.files.filter { it.isSupportedType() }
        // Drives the side nav's progress readout.
        _state.update {
            it.copy(
                filesToProcess = supportedFiles.size
            )
        }
        for (file in supportedFiles) {
            updateFile(file.id) { it.copy(status = FileItemStatus.Suggesting) }
            try {
                val cacheKey = "${model.id}:$thinking:${fileHasher.hash(file.path)}"
                val cached = suggestionCache[cacheKey]
                val result = cached ?: run {
                    val content = when (file.type) {
                        FileType.IMAGE -> ExtractedContent.Image(file.path)
                        else -> ExtractedContent.Text(textContentExtractor.extract(file.path))
                    }
                    renameSuggestionUseCase.suggest(
                        fileName = file.name,
                        content = content,
                        model = model,
                        thinking = thinking,
                    ).also { suggestionCache[cacheKey] = it }
                }

                if (cached != null) {
                    accumulateCacheHit()
                } else {
                    accumulateRequestStats(result.usage, result.durationMillis)
                }
                updateFile(file.id) {
                    it.copy(
                        status = FileItemStatus.Suggested(
                            result.suggestion.suggestedName,
                            result.suggestion.category,
                        ),
                        // A cache hit did no new work this run, so it gets no fresh cost figures —
                        // just the badge marking it as reused.
                        tokenUsage = result.usage.takeIf { cached == null },
                        durationMillis = result.durationMillis.takeIf { cached == null },
                        fromCache = cached != null,
                    )
                }
                // In auto-pilot the suggestion is applied immediately; in review it waits for the user.
                if (_state.value.mode == OrganizeMode.AUTO_PILOT) {
                    applyRenameInternal(file.id)
                }
            } catch (e: Exception) {
                // The use case already logged the model-side detail (including the malformed
                // output); this adds which file on disk it happened to.
                logger.error(e) { "No suggestion for '${file.path}' (model=${model.id})" }
                updateFile(file.id) { it.copy(status = FileItemStatus.Failed(e.toFailureMessage())) }
            }
        }
    }

    private fun accumulateRequestStats(usage: TokenUsage, durationMillis: Long) {
        _state.update {
            it.copy(
                tokenUsage = it.tokenUsage + usage,
                llmRequestCount = it.llmRequestCount + 1,
                totalDurationMillis = it.totalDurationMillis + durationMillis,
            )
        }
    }

    private fun accumulateCacheHit() {
        _state.update { it.copy(cacheHitCount = it.cacheHitCount + 1) }
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
            logger.error(e) { "Rename failed ('${file.path}' -> '${suggestion.suggestedName}')" }
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
                    logger.error(e) { "Undo failed ('${record.newPath}' -> '${record.originalPath}')" }
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

    private val supportedTypes = listOf(
        FileType.TEXT_DOCUMENT,
        FileType.CODE,
        FileType.IMAGE
    )

    private fun FileItemUi.isSupportedType(): Boolean = type in supportedTypes

    /**
     * Row-sized text for a failed file. A raw parser message ("Unexpected JSON token at offset
     * 12…") tells the user nothing they can act on, so bad model output gets a plain sentence
     * that points at the actual lever — the model choice. The parser detail is in the log.
     */
    private fun Exception.toFailureMessage(): String = when (this) {
        is ModelOutputException -> "$modelId returned an unreadable answer — try another model"
        else -> message ?: "Couldn't suggest a name"
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
