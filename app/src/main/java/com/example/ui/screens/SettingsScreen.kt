package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GeminiUiState
import com.example.ui.components.ApiKeyCard
import com.example.ui.theme.GeminiBluePrimary
import com.example.ui.theme.StatusNeutral
import com.example.ui.theme.StatusSuccess

@Composable
fun SettingsScreen(
    state: GeminiUiState,
    onNavigateToKeysSection: () -> Unit,
    onDraftChange: (slot: Int, value: String) -> Unit,
    onToggleVisibility: (slot: Int) -> Unit,
    onSaveKey: (slot: Int) -> Unit,
    onRemoveKey: (slot: Int) -> Unit,
    onTestKey: (slot: Int) -> Unit,
    onSelectModel: (String) -> Unit,
    onUpdateSystemPrompt: (String) -> Unit,
    onUpdateTemperature: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isInlineKeysExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = GeminiBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Settings & Configuration",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Section: Gemini API Keys (Access & Direct Editing)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_api_keys_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = GeminiBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Gemini API Keys",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status of keys
                        val count = listOf(state.savedKey1, state.savedKey2, state.savedKey3)
                            .count { it.isNotBlank() }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (count > 0) StatusSuccess.copy(alpha = 0.15f)
                            else StatusNeutral.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$count / 3 Configured",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (count > 0) StatusSuccess else StatusNeutral,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Access and edit the three Gemini API key fields directly here or in the dedicated section.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key Slots Quick Overview
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        KeySlotStatusRow(slot = 1, keyVal = state.savedKey1)
                        KeySlotStatusRow(slot = 2, keyVal = state.savedKey2)
                        KeySlotStatusRow(slot = 3, keyVal = state.savedKey3)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onNavigateToKeysSection,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_keys_section_button")
                        ) {
                            Text("Open Keys Section", fontSize = 13.sp)
                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = { isInlineKeysExpanded = !isInlineKeysExpanded },
                            modifier = Modifier.testTag("toggle_inline_keys_button")
                        ) {
                            Text(if (isInlineKeysExpanded) "Hide Editors" else "Edit Here", fontSize = 13.sp)
                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                imageVector = if (isInlineKeysExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Expandable inline 3-slot key editors
                    AnimatedVisibility(visible = isInlineKeysExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Inline Gemini API Key Fields",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            ApiKeyCard(
                                slot = 1,
                                draftValue = state.draftKey1,
                                savedValue = state.savedKey1,
                                isVisible = state.isKeyVisible1,
                                isTesting = state.isTestingKey1,
                                testResult = state.testResultKey1,
                                onDraftChange = { onDraftChange(1, it) },
                                onToggleVisibility = { onToggleVisibility(1) },
                                onSave = { onSaveKey(1) },
                                onRemove = { onRemoveKey(1) },
                                onTestKey = { onTestKey(1) }
                            )

                            ApiKeyCard(
                                slot = 2,
                                draftValue = state.draftKey2,
                                savedValue = state.savedKey2,
                                isVisible = state.isKeyVisible2,
                                isTesting = state.isTestingKey2,
                                testResult = state.testResultKey2,
                                onDraftChange = { onDraftChange(2, it) },
                                onToggleVisibility = { onToggleVisibility(2) },
                                onSave = { onSaveKey(2) },
                                onRemove = { onRemoveKey(2) },
                                onTestKey = { onTestKey(2) }
                            )

                            ApiKeyCard(
                                slot = 3,
                                draftValue = state.draftKey3,
                                savedValue = state.savedKey3,
                                isVisible = state.isKeyVisible3,
                                isTesting = state.isTestingKey3,
                                testResult = state.testResultKey3,
                                onDraftChange = { onDraftChange(3, it) },
                                onToggleVisibility = { onToggleVisibility(3) },
                                onSave = { onSaveKey(3) },
                                onRemove = { onRemoveKey(3) },
                                onTestKey = { onTestKey(3) }
                            )
                        }
                    }
                }
            }
        }

        // Section: Gemini Model Selection
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_model_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = GeminiBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Gemini Model",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val models = listOf(
                        "gemini-2.5-flash" to "Recommended (Fastest, latest generation)",
                        "gemini-1.5-flash" to "High-speed multimodal baseline",
                        "gemini-1.5-pro" to "Complex reasoning & deep analysis"
                    )

                    models.forEach { (modelId, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectModel(modelId) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.selectedModel == modelId,
                                onClick = { onSelectModel(modelId) }
                            )
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = modelId,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: System Instructions & Temperature
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_generation_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = GeminiBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Generation Parameters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Temperature Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Temperature: ${"%.1f".format(state.temperature)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (state.temperature < 0.4f) "Precise" else if (state.temperature < 0.8f) "Balanced" else "Creative",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = state.temperature,
                        onValueChange = onUpdateTemperature,
                        valueRange = 0.0f..1.0f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // System Instruction
                    Text(
                        text = "System Instruction",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.systemPrompt,
                        onValueChange = onUpdateSystemPrompt,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Section: Fallback Policy Architecture Info
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = GeminiBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Fallback System Policy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Empty fields: Optional, marked 'Not configured'.\n" +
                               "• 1 key entered: Uses that single key.\n" +
                               "• 2 keys entered: Rotates and fails over between the two.\n" +
                               "• 3 keys entered: Full 3-tier fallback across all configured keys.\n" +
                               "• 0 keys entered: Displays 'Gemini API key is not configured.' without crashing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun KeySlotStatusRow(slot: Int, keyVal: String) {
    val isConfig = keyVal.isNotBlank()
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isConfig) StatusSuccess else StatusNeutral)
                )
                Text(
                    text = "Gemini API Key $slot",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = if (isConfig) "Configured" else "Not configured",
                fontSize = 12.sp,
                color = if (isConfig) StatusSuccess else StatusNeutral,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
