package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FallbackLogEntry
import com.example.data.FallbackResult
import com.example.data.GeminiChatMessage
import com.example.data.GeminiKeyManager
import com.example.data.GeminiService
import com.example.data.KeyTestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeminiUiState(
    // Draft inputs for the 3 slots (initialized to empty strings)
    val draftKey1: String = "",
    val draftKey2: String = "",
    val draftKey3: String = "",

    // Saved key values
    val savedKey1: String = "",
    val savedKey2: String = "",
    val savedKey3: String = "",

    // Visibility toggles
    val isKeyVisible1: Boolean = false,
    val isKeyVisible2: Boolean = false,
    val isKeyVisible3: Boolean = false,

    // Testing statuses
    val isTestingKey1: Boolean = false,
    val isTestingKey2: Boolean = false,
    val isTestingKey3: Boolean = false,
    val testResultKey1: KeyTestResult? = null,
    val testResultKey2: KeyTestResult? = null,
    val testResultKey3: KeyTestResult? = null,

    // Studio & Chat State
    val selectedModel: String = "gemini-2.5-flash",
    val systemPrompt: String = "You are a helpful, precise Google Gemini AI assistant.",
    val temperature: Float = 0.7f,
    val isGenerating: Boolean = false,
    val chatMessages: List<GeminiChatMessage> = emptyList(),
    val promptInput: String = "",
    val fallbackLogs: List<FallbackLogEntry> = emptyList(),
    val latestFallbackTrail: List<String> = emptyList(),

    // User feedback / snackbar
    val snackbarMessage: String? = null
)

class GeminiViewModel(application: Application) : AndroidViewModel(application) {

    val keyManager = GeminiKeyManager(application)
    private val geminiService = GeminiService()

    private val _uiState = MutableStateFlow(GeminiUiState())
    val uiState: StateFlow<GeminiUiState> = _uiState.asStateFlow()

    init {
        // Observe saved keys from KeyManager
        viewModelScope.launch {
            keyManager.keysState.collect { keysMap ->
                val k1 = keysMap[1].orEmpty()
                val k2 = keysMap[2].orEmpty()
                val k3 = keysMap[3].orEmpty()
                _uiState.update { current ->
                    current.copy(
                        savedKey1 = k1,
                        savedKey2 = k2,
                        savedKey3 = k3,
                        // Sync drafts if previously unchanged
                        draftKey1 = if (current.draftKey1.isEmpty() && k1.isNotEmpty()) k1 else current.draftKey1,
                        draftKey2 = if (current.draftKey2.isEmpty() && k2.isNotEmpty()) k2 else current.draftKey2,
                        draftKey3 = if (current.draftKey3.isEmpty() && k3.isNotEmpty()) k3 else current.draftKey3
                    )
                }
            }
        }
    }

    fun onDraftKeyChanged(slot: Int, newValue: String) {
        _uiState.update {
            when (slot) {
                1 -> it.copy(draftKey1 = newValue)
                2 -> it.copy(draftKey2 = newValue)
                3 -> it.copy(draftKey3 = newValue)
                else -> it
            }
        }
    }

    fun toggleKeyVisibility(slot: Int) {
        _uiState.update {
            when (slot) {
                1 -> it.copy(isKeyVisible1 = !it.isKeyVisible1)
                2 -> it.copy(isKeyVisible2 = !it.isKeyVisible2)
                3 -> it.copy(isKeyVisible3 = !it.isKeyVisible3)
                else -> it
            }
        }
    }

    fun saveKey(slot: Int) {
        val draftValue = when (slot) {
            1 -> _uiState.value.draftKey1
            2 -> _uiState.value.draftKey2
            3 -> _uiState.value.draftKey3
            else -> ""
        }
        keyManager.saveKey(slot, draftValue)
        val msg = if (draftValue.isNotBlank()) {
            "Gemini API Key $slot saved successfully."
        } else {
            "Gemini API Key $slot is empty (Not configured)."
        }
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    fun removeKey(slot: Int) {
        keyManager.removeKey(slot)
        _uiState.update {
            val updated = when (slot) {
                1 -> it.copy(draftKey1 = "", testResultKey1 = null)
                2 -> it.copy(draftKey2 = "", testResultKey2 = null)
                3 -> it.copy(draftKey3 = "", testResultKey3 = null)
                else -> it
            }
            updated.copy(snackbarMessage = "Gemini API Key $slot removed. Marked as Not configured.")
        }
    }

    fun clearAllKeys() {
        keyManager.clearAllKeys()
        _uiState.update {
            it.copy(
                draftKey1 = "",
                draftKey2 = "",
                draftKey3 = "",
                testResultKey1 = null,
                testResultKey2 = null,
                testResultKey3 = null,
                snackbarMessage = "All Gemini API keys cleared. Marked as Not configured."
            )
        }
    }

    fun testApiKey(slot: Int) {
        val keyToTest = when (slot) {
            1 -> _uiState.value.draftKey1.ifBlank { _uiState.value.savedKey1 }
            2 -> _uiState.value.draftKey2.ifBlank { _uiState.value.savedKey2 }
            3 -> _uiState.value.draftKey3.ifBlank { _uiState.value.savedKey3 }
            else -> ""
        }.trim()

        if (keyToTest.isEmpty()) {
            // Per instructions: "If a key is empty: Do not show an error. Simply mark it as 'Not configured'."
            val notConfiguredResult = KeyTestResult(
                isConfigured = false,
                success = false,
                message = "Not configured",
                httpCode = 0
            )
            _uiState.update {
                when (slot) {
                    1 -> it.copy(testResultKey1 = notConfiguredResult, snackbarMessage = "Gemini API Key 1 is not configured.")
                    2 -> it.copy(testResultKey2 = notConfiguredResult, snackbarMessage = "Gemini API Key 2 is not configured.")
                    3 -> it.copy(testResultKey3 = notConfiguredResult, snackbarMessage = "Gemini API Key 3 is not configured.")
                    else -> it
                }
            }
            return
        }

        // Set testing indicator
        _uiState.update {
            when (slot) {
                1 -> it.copy(isTestingKey1 = true)
                2 -> it.copy(isTestingKey2 = true)
                3 -> it.copy(isTestingKey3 = true)
                else -> it
            }
        }

        viewModelScope.launch {
            val result = geminiService.testApiKey(keyToTest, _uiState.value.selectedModel)
            _uiState.update {
                val next = when (slot) {
                    1 -> it.copy(isTestingKey1 = false, testResultKey1 = result)
                    2 -> it.copy(isTestingKey2 = false, testResultKey2 = result)
                    3 -> it.copy(isTestingKey3 = false, testResultKey3 = result)
                    else -> it
                }
                val toast = if (result.success) {
                    "Key $slot Verified Active! (${result.latencyMs}ms)"
                } else {
                    "Key $slot Test: ${result.message}"
                }
                next.copy(snackbarMessage = toast)
            }
        }
    }

    fun testAllKeys() {
        for (slot in 1..3) {
            testApiKey(slot)
        }
    }

    fun onPromptInputChanged(newText: String) {
        _uiState.update { it.copy(promptInput = newText) }
    }

    fun selectModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun updateSystemPrompt(newPrompt: String) {
        _uiState.update { it.copy(systemPrompt = newPrompt) }
    }

    fun updateTemperature(temp: Float) {
        _uiState.update { it.copy(temperature = temp) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun sendPrompt(promptText: String = _uiState.value.promptInput) {
        val trimmed = promptText.trim()
        if (trimmed.isEmpty() || _uiState.value.isGenerating) return

        val userMessage = GeminiChatMessage(
            isUser = true,
            text = trimmed
        )

        _uiState.update {
            it.copy(
                promptInput = "",
                chatMessages = it.chatMessages + userMessage,
                isGenerating = true
            )
        }

        viewModelScope.launch {
            val result = geminiService.generateWithFallback(
                prompt = trimmed,
                model = _uiState.value.selectedModel,
                systemInstruction = _uiState.value.systemPrompt,
                temperature = _uiState.value.temperature,
                keyManager = keyManager
            )

            when (result) {
                is FallbackResult.Success -> {
                    val aiMessage = GeminiChatMessage(
                        isUser = false,
                        text = result.text,
                        respondedByKeySlot = result.usedKeySlot,
                        latencyMs = result.latencyMs,
                        isError = false,
                        fallbackTrail = result.trail
                    )
                    val log = FallbackLogEntry(
                        promptSnippet = trimmed.take(50),
                        attemptedSlot = result.usedKeySlot,
                        success = true,
                        details = "Answered via Key ${result.usedKeySlot} (${GeminiKeyManager.getPriorityLabel(result.usedKeySlot)}) in ${result.latencyMs}ms",
                        latencyMs = result.latencyMs
                    )
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            chatMessages = it.chatMessages + aiMessage,
                            fallbackLogs = listOf(log) + it.fallbackLogs.take(20),
                            latestFallbackTrail = result.trail
                        )
                    }
                }
                is FallbackResult.Failure -> {
                    val errorMessage = if (result.isNotConfigured) {
                        "Gemini API key is not configured."
                    } else {
                        result.errorMessage
                    }
                    val aiErrorMsg = GeminiChatMessage(
                        isUser = false,
                        text = errorMessage,
                        isError = true,
                        fallbackTrail = result.trail
                    )
                    val log = FallbackLogEntry(
                        promptSnippet = trimmed.take(50),
                        attemptedSlot = 0,
                        success = false,
                        details = if (result.isNotConfigured) "Not configured: Key 1, 2, 3 are empty" else "Failover exhausted",
                        latencyMs = 0
                    )
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            chatMessages = it.chatMessages + aiErrorMsg,
                            fallbackLogs = listOf(log) + it.fallbackLogs.take(20),
                            latestFallbackTrail = result.trail,
                            snackbarMessage = if (result.isNotConfigured) "Gemini API key is not configured." else null
                        )
                    }
                }
            }
        }
    }

    fun clearChat() {
        _uiState.update { it.copy(chatMessages = emptyList()) }
    }
}
