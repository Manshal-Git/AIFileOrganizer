package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons
import com.manshal79.aifileorganizer.presentation.organizer.FileViewMode

/** Folder breadcrumb on the left; folder + view-mode controls on the right. */
@Composable
internal fun FolderToolbar(
    folderPath: String?,
    viewMode: FileViewMode,
    canRescan: Boolean,
    onChangeFolderClick: () -> Unit,
    onRescanClick: () -> Unit,
    onSetViewMode: (FileViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BreadcrumbPill(
            folderPath = folderPath,
            modifier = Modifier.weight(1f, fill = false),
        )

        Spacer(Modifier.width(AppDimens.StackGap))

        Row(verticalAlignment = Alignment.CenterVertically) {
            ToolbarButton(
                label = "Change Folder",
                icon = AppIcons.Add,
                onClick = onChangeFolderClick,
            )
            Spacer(Modifier.width(12.dp))
            ToolbarButton(
                label = "Rescan",
                icon = AppIcons.Refresh,
                enabled = canRescan,
                onClick = onRescanClick,
            )
            Spacer(Modifier.width(16.dp))
            ViewModeToggle(viewMode = viewMode, onSetViewMode = onSetViewMode)
        }
    }
}

@Composable
private fun BreadcrumbPill(folderPath: String?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = folderPath ?: "No folder selected",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (folderPath == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        modifier = modifier
            .heightIn(min = AppDimens.MinTouchTarget)
            .pointerHoverIcon(PointerIcon.Hand),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ViewModeToggle(
    viewMode: FileViewMode,
    onSetViewMode: (FileViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ViewModeButton(
            icon = AppIcons.ViewList,
            contentDescription = "List view",
            selected = viewMode == FileViewMode.LIST,
            onClick = { onSetViewMode(FileViewMode.LIST) },
        )
        ViewModeButton(
            icon = AppIcons.GridView,
            contentDescription = "Grid view",
            selected = viewMode == FileViewMode.GRID,
            onClick = { onSetViewMode(FileViewMode.GRID) },
        )
    }
}

@Composable
private fun ViewModeButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .size(AppDimens.MinTouchTarget, 36.dp)
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .shadow(2.dp, shape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                } else {
                    Modifier
                },
            )
            // `selectable` carries Role.Tab semantics for us — a plain clickable here
            // would leave the toggle unannounced to a screen reader.
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(18.dp),
        )
    }
}
