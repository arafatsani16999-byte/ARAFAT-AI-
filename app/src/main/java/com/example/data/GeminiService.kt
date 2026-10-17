package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Tests a specific API key against Google Gemini API.
     * If key is empty/blank: returns Not Configured without throwing error.
     */
    suspend fun testApiKey(apiKey: String, model: String = "gemini-2.5-flash"): KeyTestResult =
        withContext(Dispatchers.IO) {
            val trimmed = apiKey.trim()
            if (trimmed.isEmpty()) {
                return@withContext KeyTestResult(
                    isConfigured = false,
                    success = false,
                    latencyMs = 0,
                    message = "Not configured",
                    httpCode = 0
                )
            }

            val startTime = System.currentTimeMillis()
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$trimmed"

            val bodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Ping. Say 'Gemini API Connected'.")
                            })
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 20)
                    put("temperature", 0.1)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .header("User-Agent", "GeminiStudio-Android/1.0")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val latency = System.currentTimeMillis() - startTime
                    val responseBody = response.body?.string().orEmpty()
                    val code = response.code

                    if (response.isSuccessful) {
                        KeyTestResult(
                            isConfigured = true,
                            success = true,
                            latencyMs = latency,
                            message = "Active • Valid Key (${latency}ms)",
                            httpCode = code
                        )
                    } else {
                        val parsedError = parseErrorMessage(responseBody)
                        val shortDesc = when (code) {
                            400 -> "Invalid API Key format or parameter"
                            403 -> "Forbidden: Invalid API Key or Permissions"
                            429 -> "Quota Exceeded / Rate Limited"
                            500, 503 -> "Google Gemini Service Unavailable ($code)"
                            else -> "HTTP $code"
                        }
                        KeyTestResult(
                            isConfigured = true,
                            success = false,
                            latencyMs = latency,
                            message = "$shortDesc: $parsedError",
                            httpCode = code
                        )
                    }
                }
            } catch (e: IOException) {
                val latency = System.currentTimeMillis() - startTime
                KeyTestResult(
                    isConfigured = true,
                    success = false,
                    latencyMs = latency,
                    message = "Connection error: ${e.message ?: "Failed to connect to Gemini API"}",
                    httpCode = -1
                )
            } catch (e: Exception) {
                KeyTestResult(
                    isConfigured = true,
                    success = false,
                    latencyMs = 0,
                    message = "Test error: ${e.message ?: "Unknown error"}",
                    httpCode = -1
                )
            }
        }

    /**
     * Executes prompt generation using configured keys in fallback priority:
     * Key 1 -> Key 2 -> Key 3.
     */
    suspend fun generateWithFallback(
        prompt: String,
        model: String = "gemini-2.5-flash",
        systemInstruction: String? = null,
        temperature: Float = 0.7f,
        keyManager: GeminiKeyManager
    ): FallbackResult = withContext(Dispatchers.IO) {
        val configuredKeys = keyManager.getConfiguredKeys()
        val trail = mutableListOf<String>()

        if (configuredKeys.isEmpty()) {
            trail.add("Check keys: Key 1, Key 2, Key 3 are all empty.")
            return@withContext FallbackResult.Failure(
                errorMessage = "Gemini API key is not configured.",
                isNotConfigured = true,
                trail = trail
            )
        }

        trail.add("Active keys available: ${configuredKeys.size} (Slots: ${configuredKeys.map { it.first }.joinToString(", ")})")

        for ((slot, apiKey) in configuredKeys) {
            val slotLabel = GeminiKeyManager.getSlotLabel(slot)
            val priority = GeminiKeyManager.getPriorityLabel(slot)
            trail.add("Attempting $slotLabel ($priority)...")

            val startTime = System.currentTimeMillis()
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val bodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contents)

                if (!systemInstruction.isNullOrBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                }

                put("generationConfig", JSONObject().apply {
                    put("temperature", temperature)
                    put("maxOutputTokens", 2048)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .header("User-Agent", "GeminiStudio-Android/1.0")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val latency = System.currentTimeMillis() - startTime
                    val responseBody = response.body?.string().orEmpty()
                    val code = response.code

                    if (response.isSuccessful) {
                        val text = parseGeneratedText(responseBody)
                        trail.add("$slotLabel SUCCESS (HTTP 200, ${latency}ms)")
                        return@withContext FallbackResult.Success(
                            text = text,
                            usedKeySlot = slot,
                            latencyMs = latency,
                            trail = trail
                        )
                    } else {
                        val errorMsg = parseErrorMessage(responseBody)
                        trail.add("$slotLabel FAILED (HTTP $code: $errorMsg). Failing over to next key...")
                    }
                }
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                trail.add("$slotLabel Network/IO error (${e.message}). Failing over...")
            }
        }

        // If we reach here, all configured keys failed
        trail.add("All ${configuredKeys.size} configured key(s) failed.")
        return@withContext FallbackResult.Failure(
            errorMessage = "All configured Gemini API keys failed. Check keys or network connection.",
            isNotConfigured = false,
            trail = trail
        )
    }

    private fun parseGeneratedText(jsonStr: String): String {
        return try {
            val root = JSONObject(jsonStr)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        sb.append(part.optString("text"))
                    }
                    return sb.toString().trim()
                }
            }
            "No response text received from Gemini API."
        } catch (e: Exception) {
            "Error parsing Gemini response: ${e.message}"
        }
    }

    private fun parseErrorMessage(jsonStr: String): String {
        return try {
            val root = JSONObject(jsonStr)
            val error = root.optJSONObject("error")
            if (error != null) {
                val message = error.optString("message")
                val status = error.optString("status")
                if (message.isNotEmpty()) return message
                if (status.isNotEmpty()) return status
            }
            jsonStr.take(120)
        } catch (e: Exception) {
            jsonStr.take(120)
        }
    }
}
