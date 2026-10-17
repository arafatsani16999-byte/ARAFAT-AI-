package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GeminiUiState
import com.example.ui.components.ApiKeyCard
import com.example.ui.components.FallbackFlowDiagram
import com.example.ui.theme.GeminiBluePrimary
import com.example.ui.theme.StatusError

@Composable
fun KeysScreen(
    state: GeminiUiState,
    onDraftChange: (slot: Int, value: String) -> Unit,
    onToggleVisibility: (slot: Int) -> Unit,
    onSaveKey: (slot: Int) -> Unit,
    onRemoveKey: (slot: Int) -> Unit,
    onTestKey: (slot: Int) -> Unit,
    onTestAllKeys: () -> Unit,
    onClearAllKeys: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Gemini Keys?") },
            text = {
                Text("This will remove all 3 saved API keys. The app will continue working, but Gemini features will display 'Gemini API key is not configured.'")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllKeys()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusError)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("keys_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title & Intro
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = GeminiBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Gemini API Keys",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Configure up to 3 optional Gemini API keys for seamless automatic fallback and high availability.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Visual 3-Key Fallback Pipeline
        item {
            FallbackFlowDiagram(
                k1Configured = state.savedKey1.isNotBlank(),
                k2Configured = state.savedKey2.isNotBlank(),
                k3Configured = state.savedKey3.isNotBlank()
            )
        }

        // Global Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onTestAllKeys,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_all_keys_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = "Test All Keys", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.testTag("clear_all_keys_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = StatusError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = "Clear All", fontSize = 13.sp)
                }
            }
        }

        // 1. Gemini API Key 1
        item {
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
        }

        // 2. Gemini API Key 2
        item {
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
        }

        // 3. Gemini API Key 3
        item {
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

        // Fallback Diagnostics & Activity Log
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fallback_diagnostics_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Fallback Diagnostics & Activity Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time records of prompt requests and automated key failover events.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.fallbackLogs.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No fallback activity recorded yet. Run tests or prompts in Gemini Studio to monitor failover events.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        state.fallbackLogs.take(5).forEach { entry ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (entry.attemptedSlot > 0) "Key ${entry.attemptedSlot}" else "No Configured Key",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (entry.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                        if (entry.latencyMs > 0) {
                                            Text(
                                                text = "${entry.latencyMs}ms",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = entry.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
