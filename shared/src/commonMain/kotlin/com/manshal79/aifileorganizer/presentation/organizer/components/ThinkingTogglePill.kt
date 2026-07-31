package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons
import com.manshal79.aifileorganizer.presentation.designsystem.AppTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Header switch for the selected model's thinking mode. The caller only shows it for models that
 * report the capability — Ollama rejects the flag on models that don't, so there is nothing
 * useful to offer there.
 */
@Composable
internal fun ThinkingTogglePill(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
            .heightIn(min = AppDimens.MinTouchTarget)
            .padding(start = 14.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.Lightbulb,
            contentDescription = null,
            tint = if (checked) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Thinking",
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

@Preview
@Composable
private fun ThinkingTogglePillPreview() {
    AppTheme {
        ThinkingTogglePill(checked = true, onToggle = {})
    }
}
