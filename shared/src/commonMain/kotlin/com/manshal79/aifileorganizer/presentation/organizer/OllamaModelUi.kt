package com.manshal79.aifileorganizer.presentation.organizer

import ai.koog.prompt.executor.ollama.client.nameWithoutTag
import ai.koog.prompt.llm.LLMCapability
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.domain.model.OllamaModelInfo
import com.manshal79.aifileorganizer.presentation.organizer.components.grouped
import kotlin.math.round

data class OllamaModelUi(
    val id: String,
    val displayName: String,
    val family: String,
    val parameterLabel: String?,
    val sizeLabel: String,
    val contextLengthLabel: String?,
    val quantizationLevel: String?,
    val supportsVision: Boolean,
    val supportsTools: Boolean,
    /** Reported by Ollama, not by Koog's card — see [OllamaModelInfo]. */
    val supportsThinking: Boolean,
    val supportedFileTypes: List<FileType>,
    /** This app needs vision to rename image files — models without it leave images unhandled. */
    val isRecommended: Boolean,
)

fun OllamaModelInfo.toOllamaModelUi(): OllamaModelUi {
    val supportsVision = LLMCapability.Vision.Image in card.capabilities
    val supportsTools = LLMCapability.Tools in card.capabilities

    return OllamaModelUi(
        id = card.name,
        displayName = card.nameWithoutTag,
        family = card.family,
        parameterLabel = card.parameterCount?.toParameterLabel(),
        sizeLabel = card.size.toSizeLabel(),
        contextLengthLabel = card.contextLength?.let { "${it.toInt().grouped()} tokens" },
        quantizationLevel = card.quantizationLevel,
        supportsVision = supportsVision,
        supportsTools = supportsTools,
        supportsThinking = supportsThinking,
        supportedFileTypes = buildList {
            add(FileType.TEXT_DOCUMENT)
            add(FileType.CODE)
            if (supportsVision) add(FileType.IMAGE)
        },
        isRecommended = supportsVision,
    )
}

private fun Double.roundedTo1Decimal(): Double = round(this * 10) / 10.0

private fun Long.toSizeLabel(): String = "${(this / 1_000_000_000.0).roundedTo1Decimal()} GB"

private fun Long.toParameterLabel(): String = when {
    this >= 1_000_000_000 -> "${(this / 1_000_000_000.0).roundedTo1Decimal()}B params"
    this >= 1_000_000 -> "${(this / 1_000_000.0).roundedTo1Decimal()}M params"
    else -> "$this params"
}
