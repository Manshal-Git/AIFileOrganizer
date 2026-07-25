package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons

/**
 * Top app bar: product mark on the left, the two session-wide actions on the right.
 *
 * The design's Files / History / Rules tabs are omitted — those destinations don't exist yet.
 */
@Composable
internal fun AppTopBar(
    undoCount: Int,
    canUndo: Boolean,
    canApplyAll: Boolean,
    onUndoAll: () -> Unit,
    onApplyAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.TopBarHeight)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = AppDimens.ContainerPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "AIFileOrganizer",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.primary,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = onUndoAll,
                // Disabled while there's nothing to undo — this doubles as the
                // double-tap guard, since the history empties as soon as it runs.
                enabled = canUndo,
                modifier = Modifier
                    .heightIn(min = AppDimens.MinTouchTarget)
                    .pointerHoverIcon(PointerIcon.Hand),
            ) {
                Icon(
                    imageVector = AppIcons.Undo,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Undo all ($undoCount)",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = onApplyAll,
                enabled = canApplyAll,
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
                modifier = Modifier
                    .heightIn(min = AppDimens.MinTouchTarget)
                    .pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text(
                    text = "APPLY ALL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    ),
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}
