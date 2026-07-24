package com.manshal79.aifileorganizer.presentation.organizer

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.presentation.asString
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onPickFolderClick) {
                Text("Choose Folder")
            }
            Spacer(Modifier.width(12.dp))
            Text(state.folderPath ?: "No folder selected")
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Auto-pilot")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = state.mode == OrganizeMode.AUTO_PILOT,
                onCheckedChange = { onAction(OrganizerAction.OnToggleMode) },
            )
            Spacer(Modifier.width(16.dp))
            TextButton(
                onClick = { onAction(OrganizerAction.OnToggleViewMode) },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text(if (state.viewMode == FileViewMode.LIST) "Switch to Grid" else "Switch to List")
            }
        }

        if (state.files.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onAction(OrganizerAction.OnApplyAll) },
                    enabled = state.hasApplicableSuggestions,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Text("Apply All")
                }
                Spacer(Modifier.width(12.dp))
                TextButton(
                    onClick = { onAction(OrganizerAction.OnUndoAll) },
                    enabled = state.canUndo,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Text("Undo All (${state.renameHistory.size})")
                }
            }
        }

        state.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.isScanning -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            state.files.isEmpty() -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text("No files scanned yet.")
            }

            else -> GroupedFileGrid(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun GroupedFileGrid(
    state: OrganizerState,
    onAction: (OrganizerAction) -> Unit,
) {
    val filesByType = remember(state.files) { state.files.groupBy { it.type } }
    val gridState = rememberLazyGridState()
    val columns = if (state.viewMode == FileViewMode.GRID) {
        GridCells.Adaptive(minSize = 140.dp)
    } else {
        GridCells.Fixed(1)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = columns,
            // Leave room on the right so cards don't render under the scrollbar.
            contentPadding = PaddingValues(end = 12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            FileType.entries.forEach { type ->
                val filesForType = filesByType[type].orEmpty()
                if (filesForType.isEmpty()) return@forEach

                val collapsed = type in state.collapsedTypes
                item(key = "header-${type.name}", span = { GridItemSpan(maxLineSpan) }) {
                    GroupHeader(
                        type = type,
                        count = filesForType.size,
                        collapsed = collapsed,
                        onToggle = { onAction(OrganizerAction.OnToggleGroupCollapsed(type)) },
                        modifier = Modifier.animateItem(),
                    )
                }

                if (!collapsed) {
                    items(filesForType, key = { it.id }) { file ->
                        val onApply = { onAction(OrganizerAction.OnApplyRename(file.id)) }
                        if (state.viewMode == FileViewMode.GRID) {
                            FileCard(file, onApply = onApply, modifier = Modifier.animateItem())
                        } else {
                            FileRow(file, onApply = onApply, modifier = Modifier.animateItem())
                        }
                    }
                }
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(gridState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )
    }
}

@Composable
private fun GroupHeader(
    type: FileType,
    count: Int,
    collapsed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chevronRotation by animateFloatAsState(if (collapsed) 0f else 90f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onToggle)
            .pointerHoverIcon(PointerIcon.Hand)
            .semantics { role = Role.Button }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "▸",
            modifier = Modifier
                .width(20.dp)
                .graphicsLayer { rotationZ = chevronRotation },
        )
        Text(
            text = "${type.name} ($count)",
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun FileRow(file: FileItemUi, onApply: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FileNameText(
                name = file.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            CategoryBadge(file.status)
            ApplyControl(file.status, onApply)
        }
        FileStatusRow(file.status)
    }
}

@Composable
private fun FileCard(file: FileItemUi, onApply: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            FilePreviewThumbnail(file)
            Spacer(Modifier.height(6.dp))
            FileNameText(
                name = file.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
            CategoryBadge(file.status)
            FileStatusRow(file.status)
            ApplyControl(file.status, onApply)
        }
    }
}

/** Per-file Apply/Retry button — shown only when a suggestion is ready to be (re)applied. */
@Composable
private fun ApplyControl(status: FileItemStatus, onApply: () -> Unit) {
    val label = when (status) {
        is FileItemStatus.Suggested -> "Rename"
        is FileItemStatus.RenameFailed -> "Retry"
        else -> return
    }
    TextButton(
        onClick = onApply,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
    ) {
        Text(label)
    }
}

/** File name that ellipsizes but reveals the full name in a tooltip on hover — a desktop expectation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileNameText(
    name: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(name) } },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = style,
        )
    }
}

@Composable
private fun FilePreviewThumbnail(file: FileItemUi) {
    val preview by produceState<FilePreview>(FilePreview.Unavailable, file.path, file.type) {
        value = loadFilePreview(file.path, file.type)
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = preview) { current ->
            when (current) {
                is FilePreview.Image -> Image(
                    bitmap = current.bitmap,
                    contentDescription = "Thumbnail preview of ${file.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                is FilePreview.Text -> Text(
                    text = current.snippet,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )

                FilePreview.Unavailable -> Text(
                    text = file.type.name,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun CategoryBadge(status: FileItemStatus) {
    val category = (status as? FileItemStatus.Suggested)?.category ?: return
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun FileStatusRow(status: FileItemStatus) {
    when (status) {
        FileItemStatus.Pending -> Text(
            text = "Pending",
            style = MaterialTheme.typography.labelSmall,
        )

        FileItemStatus.Suggesting -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
            )
            Spacer(Modifier.width(6.dp))
            Text("Suggesting a name…", style = MaterialTheme.typography.labelSmall)
        }

        is FileItemStatus.Suggested -> Text(
            text = "→ ${status.suggestedName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        FileItemStatus.Renaming -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
            )
            Spacer(Modifier.width(6.dp))
            Text("Renaming…", style = MaterialTheme.typography.labelSmall)
        }

        is FileItemStatus.Renamed -> Text(
            text = "✓ Renamed to ${status.newName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        is FileItemStatus.RenameFailed -> Text(
            text = "→ ${status.suggestedName}  ·  ${status.message}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )

        is FileItemStatus.Failed -> Text(
            text = status.message,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private val previewFiles = listOf(
    FileItemUi(id = "1", name = "invoice_scan.pdf", path = "/1", type = FileType.PDF),
    FileItemUi(id = "2", name = "vacation.jpg", path = "/2", type = FileType.IMAGE),
    FileItemUi(
        id = "3",
        name = "notes.txt",
        path = "/3",
        type = FileType.TEXT_DOCUMENT,
        status = FileItemStatus.Suggesting,
    ),
    FileItemUi(
        id = "4",
        name = "readme.md",
        path = "/4",
        type = FileType.TEXT_DOCUMENT,
        status = FileItemStatus.Suggested("project_setup_guide", "documentation"),
    ),
    FileItemUi(
        id = "5",
        name = "locked.txt",
        path = "/5",
        type = FileType.TEXT_DOCUMENT,
        status = FileItemStatus.Failed("Couldn't read file: permission denied"),
    ),
    FileItemUi(
        id = "6",
        name = "Main.kt",
        path = "/6",
        type = FileType.CODE,
        status = FileItemStatus.Suggested("organizer_view_model", "source"),
    ),
    FileItemUi(
        id = "7",
        name = "old_photo.png",
        path = "/7",
        type = FileType.IMAGE,
        status = FileItemStatus.Renamed("golden_gate_bridge_sunset.png"),
    ),
    FileItemUi(
        id = "8",
        name = "draft.md",
        path = "/8",
        type = FileType.TEXT_DOCUMENT,
        status = FileItemStatus.RenameFailed("meeting_notes", "notes", "target already exists"),
    ),
)

@Preview
@Composable
private fun OrganizerScreenListPreview() {
    MaterialTheme {
        OrganizerScreen(
            state = OrganizerState(
                folderPath = "/Users/manshal/Documents/Inbox",
                files = previewFiles,
            ),
            onAction = {},
            onPickFolderClick = {},
        )
    }
}

@Preview
@Composable
private fun OrganizerScreenGridPreview() {
    MaterialTheme {
        OrganizerScreen(
            state = OrganizerState(
                folderPath = "/Users/manshal/Documents/Inbox",
                viewMode = FileViewMode.GRID,
                files = previewFiles,
            ),
            onAction = {},
            onPickFolderClick = {},
        )
    }
}
