package com.aiope2.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderTemplatesTest {

  @Test
  fun `ALL list is not empty`() {
    assertTrue(ProviderTemplates.ALL.isNotEmpty())
  }

  @Test
  fun `byId map contains an entry for every provider in ALL`() {
    for (provider in ProviderTemplates.ALL) {
      assertEquals(provider, ProviderTemplates.byId[provider.id])
    }
  }

  @Test
  fun `known builtin providers are present`() {
    assertNotNull(ProviderTemplates.byId["openai"])
    assertNotNull(ProviderTemplates.byId["anthropic"])
    assertNotNull(ProviderTemplates.byId["ollama"])
    assertNotNull(ProviderTemplates.byId["custom"])
    assertNotNull(ProviderTemplates.byId["deepseek"])
    assertNotNull(ProviderTemplates.byId["openrouter"])
    assertNotNull(ProviderTemplates.byId["groq"])
    assertNotNull(ProviderTemplates.byId["pollinations"])
  }

  @Test
  fun `all providers have non-blank id and displayName`() {
    for (provider in ProviderTemplates.ALL) {
      assertTrue("Provider id should not be blank", provider.id.isNotBlank())
      assertTrue("Provider displayName should not be blank", provider.displayName.isNotBlank())
    }
  }

  @Test
  fun `ollama provider does not require API key`() {
    val ollama = ProviderTemplates.byId["ollama"]!!
    assertFalse(ollama.requiresApiKey)
  }

  @Test
  fun `pollinations provider does not require API key`() {
    val pollinations = ProviderTemplates.byId["pollinations"]!!
    assertFalse(pollinations.requiresApiKey)
  }

  @Test
  fun `custom provider does not require API key`() {
    val custom = ProviderTemplates.byId["custom"]!!
    assertFalse(custom.requiresApiKey)
  }

  @Test
  fun `openai provider requires API key`() {
    val openai = ProviderTemplates.byId["openai"]!!
    assertTrue(openai.requiresApiKey)
  }

  @Test
  fun `anthropic provider requires API key`() {
    val anthropic = ProviderTemplates.byId["anthropic"]!!
    assertTrue(anthropic.requiresApiKey)
  }

  @Test
  fun `ollama provider has local API base`() {
    val ollama = ProviderTemplates.byId["ollama"]!!
    assertEquals("http://localhost:11434/v1", ollama.apiBase)
  }

  @Test
  fun `openai provider has GPT models with vision support`() {
    val openai = ProviderTemplates.byId["openai"]!!
    assertTrue(openai.defaultModels.isNotEmpty())
    assertTrue(openai.defaultModels.any { it.supportsVision })
  }

  @Test
  fun `deepseek provider has chat and reasoner models`() {
    val deepseek = ProviderTemplates.byId["deepseek"]!!
    val ids = deepseek.defaultModels.map { it.id }
    assertTrue(ids.contains("deepseek-chat"))
    assertTrue(ids.contains("deepseek-reasoner"))
  }

  @Test
  fun `all provider ids are unique`() {
    val ids = ProviderTemplates.ALL.map { it.id }
    assertEquals(ids.size, ids.toSet().size)
  }

  @Test
  fun `google_ai_studio has a non-null API base`() {
    val google = ProviderTemplates.byId["google_ai_studio"]!!
    assertNotNull(google.apiBase)
    assertTrue(google.apiBase!!.isNotBlank())
  }

  @Test
  fun `openrouter has correct API base`() {
    val openrouter = ProviderTemplates.byId["openrouter"]!!
    assertEquals("https://openrouter.ai/api/v1", openrouter.apiBase)
  }
}
