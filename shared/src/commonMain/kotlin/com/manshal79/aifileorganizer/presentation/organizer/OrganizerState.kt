package com.manshal79.aifileorganizer.presentation.organizer

import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.model.TokenUsage
import com.manshal79.aifileorganizer.presentation.UiText

data class OrganizerState(
    val folderPath: String? = null,
    val mode: OrganizeMode = OrganizeMode.REVIEW,
    val viewMode: FileViewMode = FileViewMode.LIST,
    val files: List<FileItemUi> = emptyList(),
    val collapsedTypes: Set<FileType> = emptySet(),
    val renameHistory: List<RenameRecord> = emptyList(),
    val filesToProcess: Int = 0,
    val tokenUsage: TokenUsage = TokenUsage.Zero,
    val llmRequestCount: Int = 0,
    val totalDurationMillis: Long = 0,
    /** Suggestions reused from the cache instead of asking the model again — pure savings. */
    val cacheHitCount: Int = 0,
    val isScanning: Boolean = false,
    val error: UiText? = null,
    val ollamaUnavailable: Boolean = false,
    val modelSelectionRequired: Boolean = false,
    val availableModels: List<OllamaModelUi> = emptyList(),
    val selectedModelId: String? = null,
    val isLoadingModels: Boolean = false,
    /**
     * Off by default: naming a file is a short classification job, and reasoning traces cost
     * output tokens and seconds per file. Only has an effect on a thinking-capable model.
     */
    val thinkingEnabled: Boolean = false,
) {
    val selectedModel: OllamaModelUi?
        get() = availableModels.find { it.id == selectedModelId }

    /** Only a model that reports the capability may be asked to think — Ollama rejects the rest. */
    val thinkingSupported: Boolean
        get() = selectedModel?.supportsThinking == true

    /** What actually goes on the wire: true/false for capable models, null for the others. */
    val thinkingRequest: Boolean?
        get() = thinkingEnabled.takeIf { thinkingSupported }

    val hasApplicableSuggestions: Boolean
        get() = files.any { it.status is FileItemStatus.Suggested || it.status is FileItemStatus.RenameFailed }

    val canUndo: Boolean
        get() = renameHistory.isNotEmpty()

    val suggestionsReadyCount: Int
        get() = files.count { it.status is FileItemStatus.Suggested || it.status is FileItemStatus.RenameFailed }

    val renamedCount: Int
        get() = files.count { it.status is FileItemStatus.Renamed }

    /** Analyzable files the AI has finished with, whether it produced a name or failed. */
    val analyzedCount: Int
        get() = files.count {
            when (it.status) {
                FileItemStatus.Pending, FileItemStatus.Suggesting -> false
                else -> true
            }
        }

    /** True while any long-running work is in flight — used to gate session-wide actions. */
    val isBusy: Boolean
        get() = isScanning || files.any {
            it.status == FileItemStatus.Suggesting || it.status == FileItemStatus.Renaming
        }

    /** Last path segment of the selected folder, for the side nav header. */
    val folderName: String?
        get() = folderPath?.trimEnd('/', '\\')?.substringAfterLast('/')?.substringAfterLast('\\')
            ?.takeIf { it.isNotBlank() }
}

enum class OrganizeMode {
    REVIEW,
    AUTO_PILOT,
}

enum class FileViewMode {
    LIST,
    GRID,
}
