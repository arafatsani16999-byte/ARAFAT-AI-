package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GeminiKeyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeminiKeyManagerTest {

    private lateinit var keyManager: GeminiKeyManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        keyManager = GeminiKeyManager(context)
        keyManager.clearAllKeys()
    }

    @Test
    fun initialKeysAreEmptyAndNotConfigured() {
        assertEquals("", keyManager.getKey(1))
        assertEquals("", keyManager.getKey(2))
        assertEquals("", keyManager.getKey(3))
        assertFalse(keyManager.hasAnyConfiguredKey())
        assertTrue(keyManager.getConfiguredKeys().isEmpty())
    }

    @Test
    fun singleKeyConfigurationReturnsOneKeyInFallback() {
        keyManager.saveKey(1, "test-key-alpha")
        assertTrue(keyManager.hasAnyConfiguredKey())
        val configured = keyManager.getConfiguredKeys()
        assertEquals(1, configured.size)
        assertEquals(1 to "test-key-alpha", configured[0])
    }

    @Test
    fun twoKeysConfigurationMaintainsPriorityOrder() {
        keyManager.saveKey(1, "key-1")
        keyManager.saveKey(3, "key-3")
        val configured = keyManager.getConfiguredKeys()
        assertEquals(2, configured.size)
        assertEquals(1 to "key-1", configured[0])
        assertEquals(3 to "key-3", configured[1])
    }

    @Test
    fun removeKeyClearsSlot() {
        keyManager.saveKey(2, "key-2")
        assertEquals("key-2", keyManager.getKey(2))
        keyManager.removeKey(2)
        assertEquals("", keyManager.getKey(2))
        assertFalse(keyManager.hasAnyConfiguredKey())
    }

    @Test
    fun placeholdersMatchSpecification() {
        assertEquals("GEMINI_API_KEY_1", GeminiKeyManager.PLACEHOLDER_KEY_1)
        assertEquals("GEMINI_API_KEY_2", GeminiKeyManager.PLACEHOLDER_KEY_2)
        assertEquals("GEMINI_API_KEY_3", GeminiKeyManager.PLACEHOLDER_KEY_3)
    }
}
