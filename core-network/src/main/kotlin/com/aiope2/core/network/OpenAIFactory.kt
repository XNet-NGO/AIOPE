package com.aiope2.core.network

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAIFactory @Inject constructor() {
  fun create(provider: LlmProvider): OpenAI = OpenAI(OpenAIConfig(
    token = provider.apiKey.ifBlank { "unused" },
    host = OpenAIHost(provider.baseUrl)
  ))
}
