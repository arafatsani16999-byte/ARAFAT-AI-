package com.example.ui.components

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiKeyManager
import com.example.data.KeyTestResult
import com.example.ui.theme.GeminiBluePrimary
import com.example.ui.theme.GeminiBlueSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusNeutral
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApiKeyCard(
    slot: Int,
    draftValue: String,
    savedValue: String,
    isVisible: Boolean,
    isTesting: Boolean,
    testResult: KeyTestResult?,
    onDraftChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit,
    onTestKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val placeholder = GeminiKeyManager.getPlaceholder(slot)
    val slotTitle = GeminiKeyManager.getSlotLabel(slot)
    val priority = GeminiKeyManager.getPriorityLabel(slot)

    val isConfigured = savedValue.isNotBlank()
    val hasUnsavedChanges = draftValue != savedValue

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("api_key_card_slot_$slot"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title + Priority + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConfigured) GeminiBluePrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = if (isConfigured) GeminiBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = slotTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Priority: $priority",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                StatusBadge(
                    isConfigured = isConfigured,
                    hasUnsavedChanges = hasUnsavedChanges,
                    testResult = testResult
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Input Field
            OutlinedTextField(
                value = draftValue,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_input_$slot"),
                placeholder = {
                    Text(
                        text = placeholder,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                supportingText = {
                    Text(
                        text = GeminiKeyManager.HINT_PASTE_KEY,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Paste from clipboard helper
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                if (clipboard?.hasPrimaryClip() == true &&
                                    (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true ||
                                     clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) == true)
                                ) {
                                    val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!text.isNullOrBlank()) {
                                        onDraftChange(text.trim())
                                    }
                                }
                            },
                            modifier = Modifier.testTag("paste_key_${slot}_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste from Clipboard",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Eye toggle icon
                        IconButton(
                            onClick = onToggleVisibility,
                            modifier = Modifier.testTag("toggle_visibility_$slot")
                        ) {
                            Icon(
                                imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isVisible) "Hide API Key" else "Show API Key",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GeminiBluePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Controls: Show/Hide, Save, Remove, Test API Key
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Show/Hide API Key
                OutlinedButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.testTag("btn_toggle_visibility_$slot")
                ) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isVisible) "Hide" else "Show", fontSize = 13.sp)
                }

                // 2. Save
                FilledTonalButton(
                    onClick = onSave,
                    modifier = Modifier.testTag("save_key_${slot}_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (hasUnsavedChanges) GeminiBlueSecondary.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Save", fontSize = 13.sp)
                }

                // 3. Remove
                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove_key_${slot}_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isConfigured || draftValue.isNotEmpty()) StatusError else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Remove", fontSize = 13.sp)
                }

                // 4. Test API Key
                FilledTonalButton(
                    onClick = onTestKey,
                    enabled = !isTesting,
                    modifier = Modifier.testTag("test_key_${slot}_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GeminiBluePrimary.copy(alpha = 0.2f),
                        contentColor = GeminiBluePrimary
                    )
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = GeminiBluePrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Testing...", fontSize = 13.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Test API Key", fontSize = 13.sp)
                    }
                }
            }

            // Test Result Banner
            AnimatedVisibility(visible = testResult != null) {
                testResult?.let { res ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            !res.isConfigured -> MaterialTheme.colorScheme.surfaceVariant
                            res.success -> StatusSuccess.copy(alpha = 0.12f)
                            else -> StatusError.copy(alpha = 0.12f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    !res.isConfigured -> Icons.Outlined.Info
                                    res.success -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Warning
                                },
                                contentDescription = null,
                                tint = when {
                                    !res.isConfigured -> StatusNeutral
                                    res.success -> StatusSuccess
                                    else -> StatusError
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (!res.isConfigured) "Not configured" else res.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    !res.isConfigured -> MaterialTheme.colorScheme.onSurfaceVariant
                                    res.success -> StatusSuccess
                                    else -> StatusError
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    isConfigured: Boolean,
    hasUnsavedChanges: Boolean,
    testResult: KeyTestResult?
) {
    val (label, bg, fg) = when {
        testResult != null && !testResult.isConfigured -> {
            Triple("Not configured", StatusNeutral.copy(alpha = 0.15f), StatusNeutral)
        }
        testResult != null && testResult.success -> {
            Triple("Active (${testResult.latencyMs}ms)", StatusSuccess.copy(alpha = 0.18f), StatusSuccess)
        }
        testResult != null && !testResult.success -> {
            Triple("Test Failed", StatusError.copy(alpha = 0.18f), StatusError)
        }
        hasUnsavedChanges -> {
            Triple("Unsaved draft", StatusWarning.copy(alpha = 0.18f), StatusWarning)
        }
        isConfigured -> {
            Triple("Configured", StatusSuccess.copy(alpha = 0.18f), StatusSuccess)
        }
        else -> {
            // "If a key is empty: Do not show an error. Simply mark it as 'Not configured'."
            Triple("Not configured", StatusNeutral.copy(alpha = 0.15f), StatusNeutral)
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = Modifier.border(1.dp, fg.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = fg
            )
        }
    }
}
