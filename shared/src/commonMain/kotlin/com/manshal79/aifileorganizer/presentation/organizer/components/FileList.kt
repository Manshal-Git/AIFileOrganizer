package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons
import com.manshal79.aifileorganizer.presentation.designsystem.EyebrowTextStyle
import com.manshal79.aifileorganizer.presentation.organizer.FileItemStatus
import com.manshal79.aifileorganizer.presentation.organizer.FileItemUi
import com.manshal79.aifileorganizer.presentation.organizer.FilePreview
import com.manshal79.aifileorganizer.presentation.organizer.FileViewMode
import com.manshal79.aifileorganizer.presentation.organizer.loadFilePreview

/**
 * The white, rounded results panel: files grouped by type, each row showing the
 * original name struck through and the AI's replacement beneath it.
 */
@Composable
internal fun FileListPanel(
    files: List<FileItemUi>,
    collapsedTypes: Set<FileType>,
    viewMode: FileViewMode,
    onToggleGroup: (FileType) -> Unit,
    onApply: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filesByType = remember(files) { files.groupBy { it.type } }
    val gridState = rememberLazyGridState()
    val columns = if (viewMode == FileViewMode.GRID) {
        GridCells.Adaptive(minSize = 200.dp)
    } else {
        GridCells.Fixed(1)
    }

    ListPanelContainer(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                state = gridState,
                columns = columns,
                // Keep cards clear of the scrollbar track on the right.
                contentPadding = PaddingValues(end = 12.dp, bottom = 8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                FileType.entries.forEach { type ->
                    val filesForType = filesByType[type].orEmpty()
                    if (filesForType.isEmpty()) return@forEach

                    val collapsed = type in collapsedTypes
                    item(key = "header-${type.name}", span = { GridItemSpan(maxLineSpan) }) {
                        GroupHeader(
                            type = type,
                            count = filesForType.size,
                            collapsed = collapsed,
                            onToggle = { onToggleGroup(type) },
                            modifier = Modifier.animateItem(),
                        )
                    }

                    if (!collapsed) {
                        items(filesForType, key = { it.id }) { file ->
                            val applyThisFile = { onApply(file.id) }
                            if (viewMode == FileViewMode.GRID) {
                                FileCard(file, applyThisFile, Modifier.animateItem())
                            } else {
                                FileRow(file, applyThisFile, Modifier.animateItem())
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
}

/** Skeleton rows shown while the folder is being scanned (playbook rule 3). */
@Composable
internal fun FileListSkeleton(modifier: Modifier = Modifier) {
    ListPanelContainer(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(5) { index ->
                if (index > 0) RowDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 68.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkeletonBlock(Modifier.size(40.dp), cornerRadius = 8.dp)
                    Spacer(Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBlock(Modifier.size(width = 220.dp, height = 12.dp))
                        SkeletonBlock(Modifier.size(width = 150.dp, height = 12.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    SkeletonBlock(Modifier.size(width = 88.dp, height = 32.dp), cornerRadius = 8.dp)
                }
            }
        }
    }
}

/** Shared white card chrome so the loading, empty and populated panels share one shape. */
@Composable
private fun ListPanelContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), shape),
    ) {
        content()
    }
}

/** Centred placeholder used when there is no folder, or the folder has no files. */
@Composable
internal fun FileListEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    ListPanelContainer(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
    val chevronRotation by animateFloatAsState(
        targetValue = if (collapsed) 0f else 90f,
        label = "groupChevron",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppDimens.MinTouchTarget)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onToggle)
            .pointerHoverIcon(PointerIcon.Hand)
            .semantics {
                role = Role.Button
                contentDescription =
                    "${type.label}, $count files, ${if (collapsed) "collapsed" else "expanded"}"
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { rotationZ = chevronRotation },
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = type.label.uppercase(),
            style = EyebrowTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(10.dp))
        CountChip(count)
    }
}

@Composable
private fun CountChip(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun FileRow(
    file: FileItemUi,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val busy = file.status.isBusy
    Column(modifier = modifier.fillMaxWidth()) {
        RowDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .background(
                    if (busy) {
                        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FileIconTile(type = file.type, muted = busy)
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                OriginalNameText(
                    name = file.name,
                    struckThrough = file.status.hasReplacementName,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(2.dp))
                FileStatusLine(file.status, file.name)
            }
            Spacer(Modifier.width(16.dp))
            RowActions(status = file.status, onApply = onApply)
        }
    }
}

@Composable
private fun FileCard(
    file: FileItemUi,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), shape)
            .padding(12.dp),
    ) {
        FilePreviewThumbnail(file)
        Spacer(Modifier.height(10.dp))
        OriginalNameText(
            name = file.name,
            struckThrough = file.status.hasReplacementName,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(4.dp))
        FileStatusLine(file.status, file.name)
        Spacer(Modifier.height(10.dp))
        RowActions(status = file.status, onApply = onApply, fillWidth = true)
    }
}

@Composable
private fun FileIconTile(type: FileType, muted: Boolean) {
    val pulse = if (muted) pulseAlpha() else 1f
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { alpha = pulse }
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (muted) {
                    MaterialTheme.colorScheme.surfaceContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            )
            .semantics { contentDescription = type.accessibilityLabel },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = type.icon,
            contentDescription = null,
            tint = if (muted) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Original file name — struck through once the AI has a replacement for it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OriginalNameText(
    name: String,
    struckThrough: Boolean,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    // Names ellipsize, so the full value stays reachable via hover — a desktop expectation.
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(name) } },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        Text(
            text = name,
            style = style.copy(
                fontWeight = FontWeight.Medium,
                textDecoration = if (struckThrough) TextDecoration.LineThrough else null,
            ),
            color = if (struckThrough) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The second line of a row: the suggestion, the progress message, or the failure.
 *
 * [originalName] is only used to echo the file's extension onto the suggested base
 * name, so the row previews the actual resulting filename.
 */
@Composable
private fun FileStatusLine(status: FileItemStatus, originalName: String) {
    when (status) {
        FileItemStatus.Pending -> StatusText(
            text = "Waiting to be analyzed",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        FileItemStatus.Suggesting -> ProgressStatus("AI is suggesting a name…")

        is FileItemStatus.Suggested -> SuggestedNameLine(
            suggestedName = status.suggestedName.withExtensionOf(originalName),
            category = status.category,
        )

        FileItemStatus.Renaming -> ProgressStatus("Renaming…")

        is FileItemStatus.Renamed -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = AppIcons.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = status.newName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        is FileItemStatus.RenameFailed -> Column {
            SuggestedNameLine(
                suggestedName = status.suggestedName.withExtensionOf(originalName),
                category = status.category,
            )
            ErrorStatus(status.message)
        }

        is FileItemStatus.Failed -> ErrorStatus(status.message)
    }
}

@Composable
private fun SuggestedNameLine(suggestedName: String, category: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = AppIcons.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = suggestedName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (category.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            CategoryChip(category)
        }
    }
}

@Composable
private fun CategoryChip(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
        )
    }
}

@Composable
private fun ProgressStatus(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.graphicsLayer { alpha = 1f },
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(13.dp),
            strokeWidth = 1.5.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(8.dp))
        StatusText(text = text, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

/** Errors get an icon as well as colour so the state never rides on colour alone. */
@Composable
private fun ErrorStatus(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = AppIcons.ErrorCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusText(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Trailing action for a row.
 *
 * The button only exists in states where a rename can start; the moment it's tapped the
 * status becomes [FileItemStatus.Renaming] and the button is replaced by placeholders,
 * so a second tap can't land on it (playbook rule 8 — state-based gate).
 */
@Composable
private fun RowActions(
    status: FileItemStatus,
    onApply: () -> Unit,
    fillWidth: Boolean = false,
) {
    when (status) {
        is FileItemStatus.Suggested -> ApplyButton("Rename", onApply, fillWidth)
        is FileItemStatus.RenameFailed -> ApplyButton("Retry", onApply, fillWidth)
        FileItemStatus.Suggesting, FileItemStatus.Renaming -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonBlock(Modifier.size(width = 72.dp, height = 24.dp), cornerRadius = 6.dp)
        }

        is FileItemStatus.Renamed -> Text(
            text = "Done",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        else -> Unit
    }
}

@Composable
private fun ApplyButton(label: String, onApply: () -> Unit, fillWidth: Boolean) {
    Button(
        onClick = onApply,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 44.dp)
            .pointerHoverIcon(PointerIcon.Hand),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun FilePreviewThumbnail(file: FileItemUi) {
    val preview by produceState<FilePreview>(FilePreview.Unavailable, file.path, file.type) {
        value = loadFilePreview(file.path, file.type)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = preview, label = "filePreview") { current ->
            when (current) {
                is FilePreview.Image -> Image(
                    bitmap = current.bitmap,
                    contentDescription = "Preview of ${file.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                is FilePreview.Text -> Text(
                    text = current.snippet,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp),
                )

                FilePreview.Unavailable -> Icon(
                    imageVector = file.type.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
}

@Composable
private fun SkeletonBlock(modifier: Modifier = Modifier, cornerRadius: androidx.compose.ui.unit.Dp = 4.dp) {
    val alpha = pulseAlpha()
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            // Purely decorative — the accompanying status text is what gets announced.
            .clearAndSetSemantics {},
    )
}

/** The design's slow "pulse" keyframe, shared by every placeholder on screen. */
@Composable
private fun pulseAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )
    return alpha
}

/**
 * The AI suggests a base name only; the renamer keeps the original extension. Echo it
 * here so the row shows the filename the user will actually end up with.
 */
private fun String.withExtensionOf(originalName: String): String {
    val extension = originalName.substringAfterLast('.', "")
    return when {
        extension.isEmpty() -> this
        endsWith(".$extension", ignoreCase = true) -> this
        else -> "$this.$extension"
    }
}

private val FileItemStatus.isBusy: Boolean
    get() = this == FileItemStatus.Suggesting || this == FileItemStatus.Renaming

private val FileItemStatus.hasReplacementName: Boolean
    get() = this is FileItemStatus.Suggested ||
        this is FileItemStatus.RenameFailed ||
        this is FileItemStatus.Renamed
