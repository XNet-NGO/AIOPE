package com.aiope2.feature.chat.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiope2.core.network.LlmProvider
import com.aiope2.core.network.ProviderRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(providerStore: ProviderStore, onBack: () -> Unit) {
  var active by remember { mutableStateOf(providerStore.getActive()) }
  var editingId by remember { mutableStateOf<String?>(null) }

  if (editingId != null) {
    val provider = ProviderRegistry.get(editingId!!) ?: return
    ProviderEditor(provider, providerStore, onBack = { editingId = null; active = providerStore.getActive() })
    return
  }

  Scaffold(topBar = {
    TopAppBar(title = { Text("Settings") }, navigationIcon = {
      IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
    })
  }) { padding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
      item { Text("LLM Provider", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp)) }
      items(ProviderRegistry.ALL) { provider ->
        val isActive = provider.id == active.providerId
        val hasKey = providerStore.getApiKey(provider.id).isNotBlank() || !provider.requiresApiKey
        ListItem(
          headlineContent = { Text("${provider.icon} ${provider.displayName}") },
          supportingContent = {
            val model = providerStore.getModel(provider.id).ifBlank { provider.defaultModels.firstOrNull()?.displayName ?: "" }
            Text("$model${if (hasKey) "" else " · needs key"}", style = MaterialTheme.typography.bodySmall)
          },
          trailingContent = { if (isActive) Text("✓", color = MaterialTheme.colorScheme.primary) },
          modifier = Modifier.clickable {
            providerStore.setActiveProvider(provider.id)
            active = providerStore.getActive()
          }
        )
      }
      item {
        TextButton(onClick = { editingId = active.providerId }, modifier = Modifier.padding(16.dp)) {
          Text("Edit ${ProviderRegistry.get(active.providerId)?.displayName ?: "Provider"}")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderEditor(provider: LlmProvider, store: ProviderStore, onBack: () -> Unit) {
  var apiKey by remember { mutableStateOf(store.getApiKey(provider.id)) }
  var selectedModel by remember { mutableStateOf(store.getModel(provider.id).ifBlank { provider.defaultModels.firstOrNull()?.id ?: "" }) }
  var customUrl by remember { mutableStateOf("") }
  var showModelPicker by remember { mutableStateOf(false) }

  Scaffold(topBar = {
    TopAppBar(title = { Text("${provider.icon} ${provider.displayName}") }, navigationIcon = {
      IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
    })
  }) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
      if (provider.requiresApiKey) {
        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it },
          label = { Text("API Key (${provider.apiKeyHint})") },
          modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
      }

      // Model picker
      Text("Model", style = MaterialTheme.typography.labelMedium)
      Spacer(Modifier.height(4.dp))
      if (provider.defaultModels.isNotEmpty()) {
        provider.defaultModels.forEach { model ->
          val selected = model.id == selectedModel
          ListItem(
            headlineContent = { Text(model.displayName) },
            supportingContent = {
              val info = buildString {
                if (model.contextWindow > 0) append("${model.contextWindow / 1000}k ctx")
                if (model.supportsTools) append(" · tools")
                if (model.supportsVision) append(" · vision")
                if (model.supportsReasoning) append(" · reasoning")
                if (model.inputCostPer1M > 0) append(" · \$${model.inputCostPer1M}/\$${model.outputCostPer1M}")
              }
              Text(info.trimStart(' ', '·'), style = MaterialTheme.typography.bodySmall)
            },
            trailingContent = { if (selected) Text("✓", color = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { selectedModel = model.id }
          )
        }
      } else {
        OutlinedTextField(value = selectedModel, onValueChange = { selectedModel = it },
          label = { Text("Model ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
      }

      if (provider.id == "custom" || provider.id == "ollama") {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = customUrl, onValueChange = { customUrl = it },
          label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
          placeholder = { Text(provider.baseUrl ?: "https://api.example.com/v1") })
      }

      Spacer(Modifier.height(16.dp))
      Button(onClick = {
        if (apiKey.isNotBlank()) store.setApiKey(provider.id, apiKey)
        if (selectedModel.isNotBlank()) store.setModel(provider.id, selectedModel)
        if (customUrl.isNotBlank()) store.setCustomBaseUrl(provider.id, customUrl)
        store.setActiveProvider(provider.id)
        onBack()
      }) { Text("Save & Activate") }
    }
  }
}
