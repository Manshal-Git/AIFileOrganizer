package com.manshal79.aifileorganizer.presentation.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.model.TokenUsage
import com.manshal79.aifileorganizer.presentation.asString
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppTheme
import com.manshal79.aifileorganizer.presentation.organizer.components.AppSideNav
import com.manshal79.aifileorganizer.presentation.organizer.components.AppTopBar
import com.manshal79.aifileorganizer.presentation.organizer.components.FileListEmptyState
import com.manshal79.aifileorganizer.presentation.organizer.components.FileListPanel
import com.manshal79.aifileorganizer.presentation.organizer.components.FileListSkeleton
import com.manshal79.aifileorganizer.presentation.organizer.components.FolderToolbar
import com.manshal79.aifileorganizer.presentation.organizer.components.StatCardsRow
import com.manshal79.aifileorganizer.presentation.organizer.components.StatusToast
import com.manshal79.aifileorganizer.presentation.organizer.components.TokenUsageBar
import com.manshal79.aifileorganizer.presentation.organizer.components.ToastContent
import com.manshal79.aifileorganizer.presentation.organizer.components.ToastTone
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OrganizerRoot(
    viewModel: OrganizerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OrganizerScreen(
        state = state,
        onAction = viewModel::onAction,
        onPickFolderClick = {
            pickDirectory()?.let { path ->
                viewModel.onAction(OrganizerAction.OnFolderSelected(path))
            }
        },
    )
}

@Composable
fun OrganizerScreen(
    state: OrganizerState,
    onAction: (OrganizerAction) -> Unit,
    onPickFolderClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(
            undoCount = state.renameHistory.size,
            canUndo = state.canUndo && !state.isBusy,
            canApplyAll = state.hasApplicableSuggestions && !state.isScanning,
            onUndoAll = { onAction(OrganizerAction.OnUndoAll) },
            onApplyAll = { onAction(OrganizerAction.OnApplyAll) },
        )

        Row(modifier = Modifier.weight(1f)) {
            AppSideNav(
                workspaceName = state.folderName ?: "Workspace",
                autoPilotOn = state.mode == OrganizeMode.AUTO_PILOT,
                analyzedCount = state.analyzedCount,
                analyzableCount = state.filesToProcess,
            )

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                MainContent(
                    state = state,
                    onAction = onAction,
                    onPickFolderClick = onPickFolderClick,
                )

                StatusToast(
                    content = state.toastContent(),
                    onDismiss = { onAction(OrganizerAction.OnDismissError) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(AppDimens.ContainerPadding),
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    state: OrganizerState,
    onAction: (OrganizerAction) -> Unit,
    onPickFolderClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HeaderZone(
            autoApprove = state.mode == OrganizeMode.AUTO_PILOT,
            onToggleAutoApprove = { onAction(OrganizerAction.OnToggleMode) },
        )

        FolderToolbar(
            folderPath = state.folderPath,
            viewMode = state.viewMode,
            canRescan = state.folderPath != null && !state.isBusy,
            onChangeFolderClick = onPickFolderClick,
            onRescanClick = { onAction(OrganizerAction.OnRescanClick) },
            onSetViewMode = { onAction(OrganizerAction.OnSetViewMode(it)) },
            modifier = Modifier.padding(
                start = AppDimens.ContainerPadding,
                end = AppDimens.ContainerPadding,
                top = AppDimens.Gutter,
                bottom = AppDimens.StackGap,
            ),
        )

        FileArea(
            state = state,
            onAction = onAction,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = AppDimens.ContainerPadding),
        )

        if (state.folderPath != null) {
            if (state.llmRequestCount > 0) {
                TokenUsageBar(
                    usage = state.tokenUsage,
                    requestCount = state.llmRequestCount,
                    modifier = Modifier.padding(
                        start = AppDimens.ContainerPadding,
                        end = AppDimens.ContainerPadding,
                        top = AppDimens.StackGap,
                    ),
                )
            }
            StatCardsRow(
                suggestionsReady = state.suggestionsReadyCount,
                renamedCount = state.renamedCount,
                canApplyAll = state.hasApplicableSuggestions && !state.isScanning,
                onApplyAll = { onAction(OrganizerAction.OnApplyAll) },
                modifier = Modifier.padding(
                    horizontal = AppDimens.ContainerPadding,
                    vertical = AppDimens.Gutter,
                ),
            )
        } else {
            Spacer(Modifier.height(AppDimens.Gutter))
        }
    }
}

/** Page title plus the auto-approve switch, on the design's tinted band. */
@Composable
private fun HeaderZone(
    autoApprove: Boolean,
    onToggleAutoApprove: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(
                    horizontal = AppDimens.ContainerPadding,
                    vertical = AppDimens.Gutter,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Ready for Organization",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AutoApprovePill(checked = autoApprove, onToggle = onToggleAutoApprove)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
private fun AutoApprovePill(checked: Boolean, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
            .heightIn(min = AppDimens.MinTouchTarget)
            .padding(start = 20.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Auto approve",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(14.dp))
        // The Switch carries its own Role.Switch semantics and reads the label beside it.
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        )
    }
}

/** Routes the file area between its loading, empty and populated states. */
@Composable
private fun FileArea(
    state: OrganizerState,
    onAction: (OrganizerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isScanning -> FileListSkeleton(modifier = modifier)

        state.folderPath == null -> FileListEmptyState(
            title = "No folder selected",
            subtitle = "Choose a folder to let the AI suggest better file names.",
            modifier = modifier,
        )

        state.files.isEmpty() -> FileListEmptyState(
            title = "Nothing to organize",
            subtitle = "This folder has no files the organizer can work with.",
            modifier = modifier,
        )

        else -> FileListPanel(
            files = state.files,
            collapsedTypes = state.collapsedTypes,
            viewMode = state.viewMode,
            onToggleGroup = { onAction(OrganizerAction.OnToggleGroupCollapsed(it)) },
            onApply = { onAction(OrganizerAction.OnApplyRename(it)) },
            modifier = modifier,
        )
    }
}

/**
 * Errors take priority in the toast; otherwise it confirms how many suggestions are
 * queued. Returns null when there is nothing worth surfacing.
 */
@Composable
private fun OrganizerState.toastContent(): ToastContent? {
    val error = error
    return when {
        error != null -> ToastContent(
            title = "Something went wrong",
            detail = error.asString(),
            tone = ToastTone.ERROR,
        )

        suggestionsReadyCount > 0 && !isBusy -> ToastContent(
            title = "Ready to apply",
            detail = if (suggestionsReadyCount == 1) {
                "1 naming suggestion is waiting."
            } else {
                "$suggestionsReadyCount naming suggestions are waiting."
            },
            tone = ToastTone.INFO,
        )

        else -> null
    }
}

private val previewFiles = listOf(
    FileItemUi(
        id = "1",
        name = "Screenshot_20260510_201307.png",
        path = "/1",
        type = FileType.IMAGE,
        status = FileItemStatus.Suggested("jigsaw-puzzle-medium", "puzzles"),
        tokenUsage = TokenUsage(inputTokens = 1_842, outputTokens = 27),
    ),
    FileItemUi(
        id = "2",
        name = "Screenshot_20260510_201151.png",
        path = "/2",
        type = FileType.IMAGE,
        status = FileItemStatus.Suggested("pokemon-puzzle-medium", "puzzles"),
    ),
    FileItemUi(
        id = "3",
        name = "Screenshot_20260510_200941.png",
        path = "/3",
        type = FileType.IMAGE,
        status = FileItemStatus.Suggesting,
    ),
    FileItemUi(
        id = "4",
        name = "old_photo.png",
        path = "/4",
        type = FileType.IMAGE,
        status = FileItemStatus.Renamed("golden-gate-bridge-sunset.png"),
        tokenUsage = TokenUsage(inputTokens = 1_790, outputTokens = 31),
    ),
    FileItemUi(
        id = "5",
        name = "readme.md",
        path = "/5",
        type = FileType.TEXT_DOCUMENT,
        status = FileItemStatus.Suggested("project-setup-guide", "documentation"),
    ),
    FileItemUi(
        id = "6",
        name = "draft.md",
        path = "/6",
        type = FileType.TEXT_DOCUMENT,
        status = FileItemStatus.RenameFailed("meeting-notes", "notes", "Target already exists"),
        tokenUsage = TokenUsage(inputTokens = 640, outputTokens = 24),
    ),
    FileItemUi(
        id = "7",
        name = "locked.txt",
        path = "/7",
        type = FileType.TEXT_DOCUMENT,
        status = FileItemStatus.Failed("Couldn't read file: permission denied"),
    ),
    FileItemUi(
        id = "8",
        name = "Main.kt",
        path = "/8",
        type = FileType.CODE,
        status = FileItemStatus.Suggested("organizer-view-model", "source"),
    ),
    FileItemUi(id = "9", name = "invoice_scan.pdf", path = "/9", type = FileType.PDF),
)

private val previewState = OrganizerState(
    folderPath = "/Users/mansh/Documents/FileOrganizerTestContent/screenshots",
    files = previewFiles,
    filesToProcess = 8,
    tokenUsage = TokenUsage(inputTokens = 12_480, outputTokens = 342),
    llmRequestCount = 6,
)

@Preview
@Composable
private fun OrganizerScreenListPreview() {
    AppTheme {
        OrganizerScreen(state = previewState, onAction = {}, onPickFolderClick = {})
    }
}

@Preview
@Composable
private fun OrganizerScreenGridPreview() {
    AppTheme {
        OrganizerScreen(
            state = previewState.copy(viewMode = FileViewMode.GRID),
            onAction = {},
            onPickFolderClick = {},
        )
    }
}

@Preview
@Composable
private fun OrganizerScreenScanningPreview() {
    AppTheme {
        OrganizerScreen(
            state = OrganizerState(folderPath = "/Users/mansh/Documents/Inbox", isScanning = true),
            onAction = {},
            onPickFolderClick = {},
        )
    }
}

@Preview
@Composable
private fun OrganizerScreenEmptyPreview() {
    AppTheme {
        OrganizerScreen(state = OrganizerState(), onAction = {}, onPickFolderClick = {})
    }
}
