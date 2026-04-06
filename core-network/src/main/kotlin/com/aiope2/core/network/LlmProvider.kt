package com.aiope2.core.network

data class LlmProvider(
  val name: String,
  val baseUrl: String,
  val apiKey: String = "",
  val defaultModel: String = ""
)
