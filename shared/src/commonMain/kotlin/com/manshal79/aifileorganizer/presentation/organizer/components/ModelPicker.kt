package com.manshal79.aifileorganizer.presentation.organizer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.presentation.designsystem.AppDimens
import com.manshal79.aifileorganizer.presentation.designsystem.AppIcons
import com.manshal79.aifileorganizer.presentation.designsystem.AppTheme
import com.manshal79.aifileorganizer.presentation.organizer.OllamaModelUi

/** Header chip that opens [ModelPickerDialog]; shows just enough to know what's active. */
@Composable
internal fun ModelSelectorChip(
    selectedModel: OllamaModelUi?,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
            .heightIn(min = AppDimens.MinTouchTarget)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .semantics {
                role = Role.Button
                contentDescription = "AI model, ${selectedModel?.displayName ?: if (isLoading) "loading" else "none selected"}. " +
                    "Opens model picker."
            }
            .padding(start = 14.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading && selectedModel == null) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = AppIcons.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = selectedModel?.displayName ?: if (isLoading) "Loading models…" else "No model selected",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp).rotate(90f),
        )
    }
}

/**
 * Full-space picker over every model pulled into the local Ollama install. Only one card is
 * expanded at a time (accordion) so a model's full capability breakdown gets real room instead
 * of being crammed into a row — quick insights still show at a glance when collapsed.
 */
@Composable
internal fun ModelPickerDialog(
    models: List<OllamaModelUi>,
    selectedModelId: String?,
    isLoading: Boolean,
    onSelect: (String) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    var expandedId by remember { mutableStateOf(selectedModelId) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.sizeIn(maxWidth = 620.dp, maxHeight = 680.dp).fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose a model",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Installed via Ollama — pick which one names your files.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                    ) {
                        Icon(
                            imageVector = AppIcons.Refresh,
                            contentDescription = "Refresh model list",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                    ) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                when {
                    isLoading && models.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }

                    models.isEmpty() -> Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = AppIcons.ErrorCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No models found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Couldn't reach Ollama, or nothing is pulled yet. Run `ollama pull gemma3:4b`, " +
                                "then refresh.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(models, key = { it.id }) { model ->
                            ModelOptionCard(
                                model = model,
                                isSelected = model.id == selectedModelId,
                                isExpanded = model.id == expandedId,
                                onToggleExpand = {
                                    expandedId = if (expandedId == model.id) null else model.id
                                },
                                onSelect = { onSelect(model.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelOptionCard(
    model: OllamaModelUi,
    isSelected: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelect: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val chevronRotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "modelCardChevron")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                },
                shape = shape,
            )
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppDimens.MinTouchTarget)
                .clickable(onClick = onToggleExpand)
                .pointerHoverIcon(PointerIcon.Hand)
                .semantics {
                    role = Role.Button
                    contentDescription = "${model.displayName}, ${if (isSelected) "selected" else "not selected"}, " +
                        "${if (isExpanded) "expanded" else "collapsed"}"
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = AppIcons.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (model.isRecommended) {
                        Spacer(Modifier.width(8.dp))
                        RecommendedBadge()
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOfNotNull(model.parameterLabel, model.sizeLabel).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CapabilityDot(supported = model.supportsVision, icon = AppIcons.Image)
            Spacer(Modifier.width(6.dp))
            CapabilityDot(supported = model.supportsTools, icon = AppIcons.Bolt)
            Spacer(Modifier.width(6.dp))
            CapabilityDot(supported = model.supportsThinking, icon = AppIcons.Lightbulb)
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp).rotate(chevronRotation),
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            ModelDetails(model = model, isSelected = isSelected, onSelect = onSelect)
        }
    }
}

@Composable
private fun ModelDetails(model: OllamaModelUi, isSelected: Boolean, onSelect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(14.dp))

        Text(
            text = "Details",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        DetailRow("Family", model.family)
        model.contextLengthLabel?.let { DetailRow("Context length", it) }
        model.quantizationLevel?.let { DetailRow("Quantization", it) }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "Capabilities",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        CapabilityRow(
            label = "Vision (reads images)",
            supported = model.supportsVision,
        )
        CapabilityRow(
            label = "Tools",
            supported = model.supportsTools,
        )
        CapabilityRow(
            label = "Thinking (reasons before answering)",
            supported = model.supportsThinking,
        )

        if (model.supportsThinking) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = AppIcons.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Turn thinking on or off from the header once this model is selected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "File types it can name in this app",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(FileType.TEXT_DOCUMENT, FileType.CODE, FileType.IMAGE).forEach { type ->
                FileTypeChip(type = type, supported = type in model.supportedFileTypes)
            }
        }

        if (!model.supportsVision) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = AppIcons.ErrorCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Won't rename image files — no vision capability.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        if (isSelected) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = AppIcons.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Currently selected",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            Button(
                onClick = onSelect,
                modifier = Modifier
                    .heightIn(min = AppDimens.MinTouchTarget)
                    .pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text("Use this model", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CapabilityRow(label: String, supported: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (supported) AppIcons.CheckCircle else AppIcons.Close,
            contentDescription = if (supported) "Supported" else "Not supported",
            tint = if (supported) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Small filled/hollow dot used in the collapsed row as a glance-only capability hint. */
@Composable
private fun CapabilityDot(supported: Boolean, icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (supported) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
        },
        modifier = Modifier.size(16.dp),
    )
}

@Composable
private fun FileTypeChip(type: FileType, supported: Boolean) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(
                if (supported) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = type.icon,
            contentDescription = null,
            tint = if (supported) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (supported) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
        )
    }
}

@Composable
private fun RecommendedBadge() {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Recommended",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private val previewModels = listOf(
    OllamaModelUi(
        id = "gemma3:4b",
        displayName = "gemma3:4b",
        family = "gemma3",
        parameterLabel = "4.3B params",
        sizeLabel = "2.9 GB",
        contextLengthLabel = "40,960 tokens",
        quantizationLevel = "Q4_K_M",
        supportsVision = true,
        supportsTools = true,
        supportsThinking = false,
        supportedFileTypes = listOf(FileType.TEXT_DOCUMENT, FileType.CODE, FileType.IMAGE),
        isRecommended = true,
    ),
    OllamaModelUi(
        id = "qwen3:8b",
        displayName = "qwen3:8b",
        family = "qwen3",
        parameterLabel = "8.2B params",
        sizeLabel = "5.2 GB",
        contextLengthLabel = "40,960 tokens",
        quantizationLevel = "Q4_K_M",
        supportsVision = false,
        supportsTools = true,
        supportsThinking = true,
        supportedFileTypes = listOf(FileType.TEXT_DOCUMENT, FileType.CODE),
        isRecommended = false,
    ),
    OllamaModelUi(
        id = "llama3.2:1b",
        displayName = "llama3.2:1b",
        family = "llama",
        parameterLabel = "1.2B params",
        sizeLabel = "1.3 GB",
        contextLengthLabel = "8,192 tokens",
        quantizationLevel = "Q8_0",
        supportsVision = false,
        supportsTools = true,
        supportsThinking = false,
        supportedFileTypes = listOf(FileType.TEXT_DOCUMENT, FileType.CODE),
        isRecommended = false,
    ),
)

@Preview
@Composable
private fun ModelPickerDialogPreview() {
    AppTheme {
        ModelPickerDialog(
            models = previewModels,
            selectedModelId = "gemma3:4b",
            isLoading = false,
            onSelect = {},
            onRefresh = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun ModelSelectorChipPreview() {
    AppTheme {
        ModelSelectorChip(selectedModel = previewModels.first(), isLoading = false, onClick = {})
    }
}
