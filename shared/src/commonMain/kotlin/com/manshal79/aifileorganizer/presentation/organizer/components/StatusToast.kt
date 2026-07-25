package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manshal79.aifileorganizer.presentation.designsystem.AppColors
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons

internal enum class ToastTone { INFO, ERROR }

internal data class ToastContent(
    val title: String,
    val detail: String,
    val tone: ToastTone,
)

/**
 * Floating status card in the bottom-right corner.
 *
 * Errors reach the user here *and* on the failing row itself — the toast is never the
 * only place a failure is reported (playbook rule 4).
 */
@Composable
internal fun StatusToast(
    content: ToastContent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = content != null,
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut(),
        modifier = modifier,
    ) {
        // Hold the last non-null content so the exit animation doesn't flash empty.
        val shown = content ?: return@AnimatedVisibility
        val shape = RoundedCornerShape(12.dp)

        Row(
            modifier = Modifier
                .shadow(16.dp, shape)
                .clip(shape)
                .background(AppColors.Slate900)
                .border(1.dp, AppColors.Slate700, shape)
                .widthIn(min = 260.dp, max = 420.dp)
                .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (shown.tone == ToastTone.ERROR) AppIcons.ErrorCircle else AppIcons.CheckCircle,
                contentDescription = null,
                tint = if (shown.tone == ToastTone.ERROR) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    AppColors.PrimaryFixedDim
                },
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shown.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.SurfaceContainerLowest,
                )
                Text(
                    text = shown.detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Slate400,
                )
            }
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(AppDimens.MinTouchTarget)
                    .heightIn(min = AppDimens.MinTouchTarget)
                    .pointerHoverIcon(PointerIcon.Hand),
            ) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = "Dismiss notification",
                    tint = AppColors.Slate400,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
