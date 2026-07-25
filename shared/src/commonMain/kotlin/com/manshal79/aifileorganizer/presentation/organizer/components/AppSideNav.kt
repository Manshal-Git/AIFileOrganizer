package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.manshal79.aifileorganizer.presentation.designsystem.AppColors
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons
import com.manshal79.aifileorganizer.presentation.designsystem.EyebrowTextStyle

/**
 * Dark navigation rail.
 *
 * Only the one destination this app actually has ("Drive") is listed — the design's
 * Suggested / Categories / Settings entries and the storage-quota block are dropped,
 * since none of them exist yet. The bottom slot instead reports real analysis progress.
 */
@Composable
internal fun AppSideNav(
    workspaceName: String,
    autoPilotOn: Boolean,
    analyzedCount: Int,
    analyzableCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(AppDimens.SideNavWidth)
            .fillMaxHeight()
            .background(AppColors.Slate900)
            .padding(vertical = AppDimens.Gutter),
    ) {
        WorkspaceHeader(
            workspaceName = workspaceName,
            autoPilotOn = autoPilotOn,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.size(28.dp))

        NavItem(
            label = "Drive",
            selected = true,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Spacer(Modifier.weight(1f))

        if (analyzableCount > 0) {
            AnalysisProgressCard(
                analyzedCount = analyzedCount,
                analyzableCount = analyzableCount,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
private fun WorkspaceHeader(
    workspaceName: String,
    autoPilotOn: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.Slate800)
                .border(1.dp, AppColors.Slate700, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Folder,
                contentDescription = null,
                tint = AppColors.PrimaryFixedDim,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = workspaceName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = AppColors.SurfaceContainerLowest,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (autoPilotOn) "AUTO-PILOT ON" else "REVIEW MODE",
                style = EyebrowTextStyle,
                color = if (autoPilotOn) AppColors.PrimaryFixedDim else AppColors.Slate400,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppDimens.MinTouchTarget)
            .clip(shape)
            .background(if (selected) AppColors.Primary.copy(alpha = 0.25f) else AppColors.Slate900)
            .then(
                if (selected) Modifier.border(1.dp, AppColors.Primary.copy(alpha = 0.4f), shape) else Modifier,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.Folder,
            contentDescription = null,
            tint = if (selected) AppColors.PrimaryFixedDim else AppColors.Slate400,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (selected) AppColors.SurfaceContainerLowest else AppColors.Slate300,
        )
    }
}

/** Occupies the design's storage-quota slot with the one long-running metric we do have. */
@Composable
private fun AnalysisProgressCard(
    analyzedCount: Int,
    analyzableCount: Int,
    modifier: Modifier = Modifier,
) {
    val target = if (analyzableCount == 0) 0f else analyzedCount.toFloat() / analyzableCount
    val progress by animateFloatAsState(targetValue = target, label = "analysisProgress")
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.Slate800)
            .border(1.dp, AppColors.Slate700, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "AI Analysis",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = AppColors.Slate300,
        )
        LinearProgressIndicator(
            progress = { progress },
            color = AppColors.PrimaryFixedDim,
            trackColor = AppColors.Slate900,
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                // The label below already conveys this to a screen reader.
                .clearAndSetSemantics {},
        )
        Text(
            text = "$analyzedCount of $analyzableCount files analyzed",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.Slate400,
        )
    }
}
