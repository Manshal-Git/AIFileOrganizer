package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manshal79.aifileorganizer.domain.model.TokenUsage
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons
import com.manshal79.aifileorganizer.presentation.designsystem.EyebrowTextStyle

/**
 * Running token spend for the session, sitting between the file list and the stat cards.
 *
 * The figures come from the provider's own accounting (Ollama's `prompt_eval_count` /
 * `eval_count`), not from an estimate, so they can be trusted for sizing prompts.
 */
@Composable
internal fun TokenUsageBar(
    usage: TokenUsage,
    requestCount: Int,
    totalDurationMillis: Long,
    cacheHitCount: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    // The figures read badly one cell at a time, so the whole strip is announced as a sentence.
    val spokenSummary = "AI usage: ${usage.inputTokens} input tokens, ${usage.outputTokens} " +
        "output tokens, ${usage.totalTokens} total across $requestCount requests, " +
        "${totalDurationMillis.formatDuration()} elapsed" +
        if (cacheHitCount > 0) ", $cacheHitCount reused from cache" else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), shape)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clearAndSetSemantics { contentDescription = spokenSummary },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "AI USAGE",
            style = EyebrowTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            UsageFigure(label = "Input", value = usage.inputTokens)
            UsageFigure(label = "Output", value = usage.outputTokens)
            UsageFigure(label = "Total", value = usage.totalTokens)
            TimeFigure(label = "Time", value = totalDurationMillis)
        }

        Spacer(Modifier.width(20.dp))
        Text(
            text = if (requestCount == 1) "1 request" else "$requestCount requests",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (cacheHitCount > 0) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = "· $cacheHitCount cached",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun UsageFigure(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = value.grouped(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TimeFigure(label: String, value: Long) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = value.formatDuration(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** `12480` -> `12,480`. Common code has no `String.format`, so group the digits by hand. */
internal fun Int.grouped(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()

/** `340` -> `340ms`, `1900` -> `1.9s`, `75_000` -> `1m 15s`. */
internal fun Long.formatDuration(): String {
    val totalMs = this
    return when {
        totalMs < 1_000 -> "${totalMs}ms"
        totalMs < 60_000 -> {
            val tenths = totalMs / 100
            "${tenths / 10}.${tenths % 10}s"
        }
        else -> {
            val totalSeconds = totalMs / 1_000
            "${totalSeconds / 60}m ${totalSeconds % 60}s"
        }
    }
}
