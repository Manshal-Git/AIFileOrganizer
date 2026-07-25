package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manshal79.aifileorganizer.presentation.designsystem.AppColors
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons
import com.manshal79.aifileorganizer.presentation.designsystem.EyebrowTextStyle

/**
 * The three summary cards under the file list.
 *
 * The design's figures (accuracy %, hours saved) are invented metrics this app doesn't
 * track, so the cards carry counts derived from the current session instead.
 */
@Composable
internal fun StatCardsRow(
    suggestionsReady: Int,
    renamedCount: Int,
    canApplyAll: Boolean,
    onApplyAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 116.dp),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Gutter),
    ) {
        MetricCard(
            eyebrow = "SUGGESTIONS READY",
            value = suggestionsReady.toString(),
            caption = "Awaiting your approval",
            icon = AppIcons.TrendingUp,
            iconTint = AppColors.PrimaryFixedDim,
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            eyebrow = "RENAMED",
            value = renamedCount.toString(),
            caption = "Applied this session",
            icon = AppIcons.Bolt,
            iconTint = AppColors.TertiaryFixedDim,
            modifier = Modifier.weight(1f),
        )
        BatchCard(
            suggestionsReady = suggestionsReady,
            enabled = canApplyAll,
            onApplyAll = onApplyAll,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricCard(
    eyebrow: String,
    value: String,
    caption: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(AppColors.Slate900)
            .border(1.dp, AppColors.Slate800, shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = eyebrow, style = EyebrowTextStyle, color = AppColors.Slate400)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
            ),
            color = AppColors.SurfaceContainerLowest,
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Slate400,
        )
    }
}

/** Primary-coloured CTA card — the same action as the top bar's Apply All. */
@Composable
private fun BatchCard(
    suggestionsReady: Int,
    enabled: Boolean,
    onApplyAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) AppColors.Primary else AppColors.SurfaceDim)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onApplyAll,
            )
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default),
    ) {
        // Oversized watermark bleeding off the bottom-right corner, as in the design.
        Icon(
            imageVector = AppIcons.AutoAwesome,
            contentDescription = null,
            tint = Color.White.copy(alpha = if (enabled) 0.18f else 0.35f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 18.dp, y = 18.dp)
                .size(110.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "ACTIVE BATCH",
                    style = EyebrowTextStyle,
                    color = if (enabled) AppColors.PrimaryFixedDim else AppColors.OnSurfaceVariant,
                )
                Icon(
                    imageVector = AppIcons.AutoAwesome,
                    contentDescription = null,
                    tint = if (enabled) Color.White else AppColors.OnSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Apply All",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                ),
                color = if (enabled) Color.White else AppColors.OnSurfaceVariant,
            )
            Text(
                text = if (suggestionsReady == 1) {
                    "1 file ready for renaming"
                } else {
                    "$suggestionsReady files ready for renaming"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) AppColors.PrimaryFixedDim else AppColors.OnSurfaceVariant,
            )
        }
    }
}
