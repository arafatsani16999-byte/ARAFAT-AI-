package com.example.data

import java.util.UUID

data class KeyTestResult(
    val timestamp: Long = System.currentTimeMillis(),
    val isConfigured: Boolean,
    val success: Boolean,
    val latencyMs: Long = 0,
    val message: String,
    val httpCode: Int = 0
)

data class FallbackLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val promptSnippet: String,
    val attemptedSlot: Int,
    val success: Boolean,
    val details: String,
    val latencyMs: Long = 0
)

data class GeminiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val respondedByKeySlot: Int? = null,
    val latencyMs: Long? = null,
    val isError: Boolean = false,
    val fallbackTrail: List<String> = emptyList()
)

sealed class FallbackResult {
    data class Success(
        val text: String,
        val usedKeySlot: Int,
        val latencyMs: Long,
        val trail: List<String>
    ) : FallbackResult()

    data class Failure(
        val errorMessage: String,
        val isNotConfigured: Boolean = false,
        val trail: List<String> = emptyList()
    ) : FallbackResult()
}
