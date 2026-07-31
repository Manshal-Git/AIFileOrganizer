package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons

/**
 * Full-width, solid-color alert for a blocking condition (Ollama unreachable) — deliberately
 * louder than [StatusToast], which is easy to miss in the corner and auto-competes with other
 * toasts. Stays up until the user retries or dismisses it.
 */
@Composable
internal fun OllamaUnavailableBanner(
    onRetryClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertBanner(
        icon = AppIcons.ErrorCircle,
        title = "Ollama isn't running",
        message = "Can't reach it at localhost:11434. Start it with `ollama serve`, then retry.",
        actionLabel = "Retry",
        actionIcon = AppIcons.Refresh,
        onAction = onRetryClick,
        onDismiss = onDismiss,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier,
    )
}

/**
 * Shown once a scan finds files it could suggest names for, but no model has been picked yet.
 * No model is ever auto-selected (loading a heavy one onto the user's machine without asking
 * would be a bad surprise), so this nudges the user to the picker instead of silently stalling.
 */
@Composable
internal fun SelectModelBanner(
    onChooseModelClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertBanner(
        icon = AppIcons.AutoAwesome,
        title = "Choose a model to continue",
        message = "Ollama is running, but no model is selected yet. Pick one to start naming files.",
        actionLabel = "Choose model",
        actionIcon = AppIcons.AutoAwesome,
        onAction = onChooseModelClick,
        onDismiss = onDismiss,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier,
    )
}

@Composable
private fun AlertBanner(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String,
    actionIcon: ImageVector,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(horizontal = AppDimens.ContainerPadding, vertical = 14.dp)
            .semantics { liveRegion = LiveRegionMode.Assertive },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        }
        Spacer(Modifier.width(16.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = contentColor,
                contentColor = containerColor,
            ),
            modifier = Modifier
                .heightIn(min = AppDimens.MinTouchTarget)
                .pointerHoverIcon(PointerIcon.Hand),
        ) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(actionLabel, style = MaterialTheme.typography.labelLarge)
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(AppDimens.MinTouchTarget)
                .pointerHoverIcon(PointerIcon.Hand),
        ) {
            Icon(
                imageVector = AppIcons.Close,
                contentDescription = "Dismiss notification",
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
