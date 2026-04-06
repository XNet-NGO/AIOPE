package com.aiope2.core.network

data class ModelDef(
  val id: String,
  val displayName: String,
  val contextWindow: Int = 0,
  val supportsTools: Boolean = true,
  val supportsVision: Boolean = false,
  val supportsReasoning: Boolean = false,
  val inputCostPer1M: Double = 0.0,
  val outputCostPer1M: Double = 0.0
)

data class LlmProvider(
  val id: String,
  val displayName: String,
  val icon: String = "🔌",
  val baseUrl: String? = null,
  val apiKeyHint: String = "",
  val requiresApiKey: Boolean = true,
  val supportsTools: Boolean = true,
  val supportsVision: Boolean = false,
  val defaultModels: List<ModelDef> = emptyList()
)
