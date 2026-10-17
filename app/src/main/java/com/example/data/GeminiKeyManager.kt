package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeminiKeyManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "gemini_api_keys_prefs"
        private const val PREF_KEY_1 = "gemini_key_slot_1"
        private const val PREF_KEY_2 = "gemini_key_slot_2"
        private const val PREF_KEY_3 = "gemini_key_slot_3"

        // Required placeholders as specified in instructions
        const val PLACEHOLDER_KEY_1 = "GEMINI_API_KEY_1"
        const val PLACEHOLDER_KEY_2 = "GEMINI_API_KEY_2"
        const val PLACEHOLDER_KEY_3 = "GEMINI_API_KEY_3"
        const val HINT_PASTE_KEY = "Paste Gemini API Key Here"

        fun getPlaceholder(slot: Int): String = when (slot) {
            1 -> PLACEHOLDER_KEY_1
            2 -> PLACEHOLDER_KEY_2
            3 -> PLACEHOLDER_KEY_3
            else -> "GEMINI_API_KEY"
        }

        fun getSlotLabel(slot: Int): String = "Gemini API Key $slot"

        fun getPriorityLabel(slot: Int): String = when (slot) {
            1 -> "Primary"
            2 -> "Fallback 1"
            3 -> "Fallback 2"
            else -> "Fallback"
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In-memory reactive state holding current saved keys.
    // Initialized to completely empty strings.
    private val _keysState = MutableStateFlow(loadKeysFromPrefs())
    val keysState: StateFlow<Map<Int, String>> = _keysState.asStateFlow()

    private fun loadKeysFromPrefs(): Map<Int, String> {
        return mapOf(
            1 to (prefs.getString(PREF_KEY_1, "") ?: ""),
            2 to (prefs.getString(PREF_KEY_2, "") ?: ""),
            3 to (prefs.getString(PREF_KEY_3, "") ?: "")
        )
    }

    fun getKey(slot: Int): String {
        return _keysState.value[slot].orEmpty()
    }

    fun saveKey(slot: Int, rawKey: String) {
        val trimmed = rawKey.trim()
        val prefKey = when (slot) {
            1 -> PREF_KEY_1
            2 -> PREF_KEY_2
            3 -> PREF_KEY_3
            else -> return
        }
        prefs.edit().putString(prefKey, trimmed).apply()
        _keysState.value = loadKeysFromPrefs()
    }

    fun removeKey(slot: Int) {
        val prefKey = when (slot) {
            1 -> PREF_KEY_1
            2 -> PREF_KEY_2
            3 -> PREF_KEY_3
            else -> return
        }
        prefs.edit().remove(prefKey).apply()
        _keysState.value = loadKeysFromPrefs()
    }

    fun clearAllKeys() {
        prefs.edit().clear().apply()
        _keysState.value = loadKeysFromPrefs()
    }

    /**
     * Returns a list of configured (non-blank) keys in priority order:
     * Slot 1 (Primary), Slot 2 (Fallback 1), Slot 3 (Fallback 2).
     */
    fun getConfiguredKeys(): List<Pair<Int, String>> {
        val state = _keysState.value
        val list = mutableListOf<Pair<Int, String>>()
        for (slot in 1..3) {
            val key = state[slot].orEmpty().trim()
            if (key.isNotEmpty()) {
                list.add(slot to key)
            }
        }
        return list
    }

    fun hasAnyConfiguredKey(): Boolean {
        return getConfiguredKeys().isNotEmpty()
    }
}
