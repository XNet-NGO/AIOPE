package com.aiope2.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderProfileTest {

  @Test
  fun `toJson includes all basic fields`() {
    val profile = ProviderProfile(
      id = "test-id",
      builtinId = "openai",
      label = "My OpenAI",
      apiKey = "sk-test",
      apiBase = "https://api.openai.com/v1",
      selectedModelId = "gpt-4o",
      isActive = true,
    )
    val json = profile.toJson()
    assertEquals("test-id", json.getString("id"))
    assertEquals("openai", json.getString("builtinId"))
    assertEquals("My OpenAI", json.getString("label"))
    assertEquals("sk-test", json.getString("apiKey"))
    assertEquals("https://api.openai.com/v1", json.getString("apiBase"))
    assertEquals("gpt-4o", json.getString("selectedModelId"))
    assertTrue(json.getBoolean("isActive"))
  }

  @Test
  fun `fromJson roundtrip preserves all basic fields`() {
    val original = ProviderProfile(
      id = "profile-123",
      builtinId = "anthropic",
      label = "Anthropic",
      apiKey = "sk-ant-key",
      apiBase = "",
      selectedModelId = "claude-3-5-haiku-20241022",
      isActive = false,
    )
    val restored = ProviderProfile.fromJson(original.toJson())
    assertEquals(original.id, restored.id)
    assertEquals(original.builtinId, restored.builtinId)
    assertEquals(original.label, restored.label)
    assertEquals(original.apiKey, restored.apiKey)
    assertEquals(original.apiBase, restored.apiBase)
    assertEquals(original.selectedModelId, restored.selectedModelId)
    assertEquals(original.isActive, restored.isActive)
  }

  @Test
  fun `effectiveApiBase returns explicit apiBase when non-blank`() {
    val profile = ProviderProfile(builtinId = "openai", apiBase = "https://custom.api.com/v1")
    assertEquals("https://custom.api.com/v1", profile.effectiveApiBase())
  }

  @Test
  fun `effectiveApiBase falls back to builtin template apiBase when blank`() {
    val profile = ProviderProfile(builtinId = "ollama", apiBase = "")
    assertEquals("http://localhost:11434/v1", profile.effectiveApiBase())
  }

  @Test
  fun `effectiveApiBase returns empty string for unknown builtinId with blank apiBase`() {
    val profile = ProviderProfile(builtinId = "unknown_provider", apiBase = "")
    assertEquals("", profile.effectiveApiBase())
  }

  @Test
  fun `effectiveModel returns selectedModelId`() {
    val profile = ProviderProfile(selectedModelId = "claude-3-5-haiku-20241022")
    assertEquals("claude-3-5-haiku-20241022", profile.effectiveModel())
  }

  @Test
  fun `activeModelConfig returns existing config when present`() {
    val config = ModelConfig(modelId = "gpt-4o", temperature = 0.9f)
    val profile = ProviderProfile(
      selectedModelId = "gpt-4o",
      modelConfigs = mapOf("gpt-4o" to config),
    )
    assertEquals(config, profile.activeModelConfig())
  }

  @Test
  fun `activeModelConfig creates default config when none exists for selected model`() {
    val profile = ProviderProfile(selectedModelId = "new-model")
    val config = profile.activeModelConfig()
    assertEquals("new-model", config.modelId)
  }

  @Test
  fun `fromJson roundtrip preserves nested modelConfigs`() {
    val modelConfig = ModelConfig(modelId = "gpt-4o", temperature = 0.8f, maxTokens = 2048)
    val profile = ProviderProfile(
      id = "p1",
      selectedModelId = "gpt-4o",
      modelConfigs = mapOf("gpt-4o" to modelConfig),
    )
    val restored = ProviderProfile.fromJson(profile.toJson())
    assertNotNull(restored.modelConfigs["gpt-4o"])
    assertEquals(0.8f, restored.modelConfigs["gpt-4o"]?.temperature)
    assertEquals(2048, restored.modelConfigs["gpt-4o"]?.maxTokens)
  }

  @Test
  fun `fromJson with empty modelConfigs results in empty map`() {
    val profile = ProviderProfile(id = "p2", selectedModelId = "model", modelConfigs = emptyMap())
    val restored = ProviderProfile.fromJson(profile.toJson())
    assertTrue(restored.modelConfigs.isEmpty())
  }

  @Test
  fun `new ProviderProfile has non-blank generated id`() {
    val profile = ProviderProfile()
    assertTrue(profile.id.isNotBlank())
  }

  @Test
  fun `two default ProviderProfiles have different ids`() {
    val p1 = ProviderProfile()
    val p2 = ProviderProfile()
    assertFalse(p1.id == p2.id)
  }

  @Test
  fun `isActive defaults to false`() {
    val profile = ProviderProfile()
    assertFalse(profile.isActive)
  }

  @Test
  fun `effectiveApiBase for google_ai_studio uses builtin template`() {
    val profile = ProviderProfile(builtinId = "google_ai_studio", apiBase = "")
    assertEquals("https://generativelanguage.googleapis.com/v1beta/openai", profile.effectiveApiBase())
  }

  @Test
  fun `effectiveApiBase for openrouter uses builtin template`() {
    val profile = ProviderProfile(builtinId = "openrouter", apiBase = "")
    assertEquals("https://openrouter.ai/api/v1", profile.effectiveApiBase())
  }
}
