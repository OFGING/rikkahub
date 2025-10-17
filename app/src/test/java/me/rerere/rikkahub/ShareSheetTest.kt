package me.rerere.rikkahub

import me.rerere.ai.provider.ProviderSetting
import me.rerere.rikkahub.ui.components.ui.decodeProviderSetting
import me.rerere.rikkahub.ui.components.ui.encodeForShare
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.uuid.Uuid

class ShareSheetTest {
    @Test
    fun `decode should restore Google provider correctly`() {
        val originalId = Uuid.random()
        val original = ProviderSetting.Google(
            id = originalId,
            enabled = true,
            name = "Test Google",
            models = emptyList(),
            apiKey = "test-google-key",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            vertexAI = false
        )

        val encoded = original.encodeForShare()
        val decoded = decodeProviderSetting(encoded)

        assertTrue(decoded is ProviderSetting.Google)
        val decodedGoogle = decoded as ProviderSetting.Google
        assertEquals(originalId, decodedGoogle.id)
        assertEquals("Test Google", decodedGoogle.name)
        assertEquals("test-google-key", decodedGoogle.apiKey)
        assertEquals(false, decodedGoogle.vertexAI)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decode should throw exception for invalid prefix`() {
        decodeProviderSetting("invalid-string")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decode should throw exception for wrong version`() {
        decodeProviderSetting("ai-provider:v2:somedata")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decode should throw exception for invalid base64`() {
        decodeProviderSetting("ai-provider:v1:not-valid-base64!!!")
    }

    @Test
    fun `encode and decode should be reversible`() {
        val providers = listOf(
            ProviderSetting.Google(
                name = "Google Test",
                apiKey = "key2",
                vertexAI = true,
                projectId = "project-123"
            )
        )

        providers.forEach { original ->
            val encoded = original.encodeForShare()
            val decoded = decodeProviderSetting(encoded)

            assertEquals(original.id, decoded.id)
            assertEquals(original.name, decoded.name)
            assertEquals(original.enabled, decoded.enabled)
        }
    }
}
