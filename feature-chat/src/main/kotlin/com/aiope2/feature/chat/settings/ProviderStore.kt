package com.aiope2.feature.chat.settings

import android.content.Context
import com.aiope2.core.network.LlmProvider
import com.aiope2.core.network.ProviderRegistry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveProvider(
  val providerId: String,
  val modelId: String,
  val apiKey: String = "",
  val customBaseUrl: String = ""
) {
  fun resolve(): LlmProvider? = ProviderRegistry.get(providerId)
  fun baseUrl(): String = customBaseUrl.ifBlank { resolve()?.baseUrl ?: "https://api.openai.com/v1" }
}

@Singleton
class ProviderStore @Inject constructor(@ApplicationContext ctx: Context) {
  private val prefs = ctx.getSharedPreferences("providers", Context.MODE_PRIVATE)

  fun getActive(): ActiveProvider {
    val pid = prefs.getString("active_id", "pollinations") ?: "pollinations"
    val provider = ProviderRegistry.get(pid) ?: ProviderRegistry.ALL.first()
    return ActiveProvider(
      providerId = pid,
      modelId = prefs.getString("model_$pid", provider.defaultModels.firstOrNull()?.id ?: "") ?: "",
      apiKey = prefs.getString("key_$pid", "") ?: "",
      customBaseUrl = prefs.getString("url_$pid", "") ?: ""
    )
  }

  fun setActiveProvider(id: String) {
    prefs.edit().putString("active_id", id).apply()
  }

  fun setApiKey(providerId: String, key: String) {
    prefs.edit().putString("key_$providerId", key).apply()
  }

  fun setModel(providerId: String, modelId: String) {
    prefs.edit().putString("model_$providerId", modelId).apply()
  }

  fun setCustomBaseUrl(providerId: String, url: String) {
    prefs.edit().putString("url_$providerId", url).apply()
  }

  fun getApiKey(providerId: String): String = prefs.getString("key_$providerId", "") ?: ""
  fun getModel(providerId: String): String = prefs.getString("model_$providerId", "") ?: ""
}
