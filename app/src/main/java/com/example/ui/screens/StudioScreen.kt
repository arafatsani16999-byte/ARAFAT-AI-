package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiChatMessage
import com.example.data.GeminiKeyManager
import com.example.ui.GeminiUiState
import com.example.ui.theme.GeminiBluePrimary
import com.example.ui.theme.GeminiBlueSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusNeutral
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun StudioScreen(
    state: GeminiUiState,
    onPromptChange: (String) -> Unit,
    onSendPrompt: (String) -> Unit,
    onNavigateToKeys: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val configuredKeysCount = listOf(state.savedKey1, state.savedKey2, state.savedKey3)
        .count { it.isNotBlank() }
    val hasKeys = configuredKeysCount > 0

    // Auto-scroll on new message
    LaunchedEffect(state.chatMessages.size) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("studio_screen")
    ) {
        // Unconfigured Banner if no keys are present
        if (!hasKeys) {
            Surface(
                color = StatusWarning.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StatusWarning.copy(alpha = 0.3f))
                    .testTag("unconfigured_warning_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyOff,
                            contentDescription = null,
                            tint = StatusWarning,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Gemini API key is not configured.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning
                            )
                            Text(
                                text = "Keys 1, 2, and 3 are empty. Set up keys to enable responses.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onNavigateToKeys,
                        modifier = Modifier.testTag("banner_configure_keys_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StatusWarning.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Add Keys", fontSize = 12.sp)
                    }
                }
            }
        } else {
            // Active Redundancy Pill Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Model: ${state.selectedModel}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• $configuredKeysCount/3 Keys Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess
                        )
                    }

                    if (state.chatMessages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Chat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chat Message List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.chatMessages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GeminiBluePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GeminiBluePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Google Gemini Workspace",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (!hasKeys)
                            "Gemini API key is not configured.\nEnter your keys in the Gemini API Keys section to test live responses."
                        else
                            "Ask questions, summarize documents, debug code, and experience automated 3-key fallback rotation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!hasKeys) {
                        FilledTonalButton(
                            onClick = onNavigateToKeys,
                            modifier = Modifier.testTag("empty_state_add_keys_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configure Gemini API Keys")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.chatMessages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            onCopyText = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("Gemini Response", text)
                                clipboard?.setPrimaryClip(clip)
                            }
                        )
                    }

                    if (state.isGenerating) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = GeminiBluePrimary
                                )
                                Text(
                                    text = "Gemini is thinking...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Suggestions Bar
        val quickPrompts = listOf(
            "💡 Summarize key concepts",
            "🚀 Explain quantum computing",
            "⚡ Test API Key response",
            "💻 Write Kotlin Flow example",
            "✨ Creative brainstorming"
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.clickable {
                        onSendPrompt(prompt.substringAfter(" "))
                    }
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Bottom Prompt Input Bar
        Surface(
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.promptInput,
                    onValueChange = onPromptChange,
                    placeholder = {
                        Text(
                            text = if (hasKeys) "Ask Gemini anything..." else "Configure API key to send...",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prompt_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBluePrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                FilledIconButton(
                    onClick = { onSendPrompt(state.promptInput) },
                    enabled = state.promptInput.isNotBlank() && !state.isGenerating,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_prompt_button")
                ) {
                    if (state.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Prompt"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: GeminiChatMessage,
    onCopyText: (String) -> Unit
) {
    var isTrailExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        // Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = when {
                message.isUser -> GeminiBlueSecondary.copy(alpha = 0.2f)
                message.isError -> StatusError.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier
                .fillMaxWidth(if (message.isUser) 0.85f else 0.95f)
                .border(
                    1.dp,
                    when {
                        message.isUser -> GeminiBlueSecondary.copy(alpha = 0.4f)
                        message.isError -> StatusError.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    },
                    RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header of message
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (message.isUser) "You" else "Google Gemini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (message.isUser) GeminiBlueSecondary else GeminiBluePrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        message.respondedByKeySlot?.let { slot ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Key $slot (${GeminiKeyManager.getPriorityLabel(slot)})",
                                    fontSize = 10.sp,
                                    color = StatusSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        message.latencyMs?.let { latency ->
                            Text(
                                text = "⚡ ${latency}ms",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!message.isUser) {
                            IconButton(
                                onClick = { onCopyText(message.text) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isError) StatusError else MaterialTheme.colorScheme.onSurface
                )

                // Expandable fallback trail if available
                if (message.fallbackTrail.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isTrailExpanded = !isTrailExpanded }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTrailExpanded) "Hide routing log" else "Show routing log (${message.fallbackTrail.size} steps)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    AnimatedVisibility(visible = isTrailExpanded) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                message.fallbackTrail.forEach { step ->
                                    Text(
                                        text = "• $step",
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
