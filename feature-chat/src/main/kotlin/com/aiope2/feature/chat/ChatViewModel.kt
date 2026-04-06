package com.aiope2.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages = _messages.asStateFlow()

  private val _isStreaming = MutableStateFlow(false)
  val isStreaming = _isStreaming.asStateFlow()

  private val _terminalVisible = MutableStateFlow(false)
  val terminalVisible = _terminalVisible.asStateFlow()

  // Default: Pollinations (free, no key)
  private val openai = OpenAI(OpenAIConfig(
    token = "unused",
    host = OpenAIHost("https://text.pollinations.ai/v1/")
  ))

  fun send(text: String) {
    val userMsg = ChatMessage(role = Role.USER, content = text)
    _messages.value = _messages.value + userMsg

    viewModelScope.launch {
      _isStreaming.value = true
      try {
        val assistantMsg = ChatMessage(role = Role.ASSISTANT, content = "")
        _messages.value = _messages.value + assistantMsg

        val request = ChatCompletionRequest(
          model = ModelId("openai-fast"),
          messages = _messages.value.filter { it.role != Role.TOOL }.map { msg ->
            com.aallam.openai.api.chat.ChatMessage(
              role = when (msg.role) {
                Role.USER -> ChatRole.User
                Role.ASSISTANT -> ChatRole.Assistant
                Role.SYSTEM -> ChatRole.System
                Role.TOOL -> ChatRole.Tool
              },
              content = msg.content
            )
          }
        )

        val chunks = openai.chatCompletions(request)
        val sb = StringBuilder()
        chunks.collect { chunk ->
          chunk.choices.firstOrNull()?.delta?.content?.let { delta ->
            sb.append(delta)
            // Update last message in-place
            val updated = _messages.value.toMutableList()
            updated[updated.lastIndex] = updated.last().copy(content = sb.toString())
            _messages.value = updated
          }
        }
      } catch (e: Exception) {
        val errMsg = ChatMessage(role = Role.ASSISTANT, content = "Error: ${e.message}")
        val updated = _messages.value.toMutableList()
        updated[updated.lastIndex] = errMsg
        _messages.value = updated
      } finally {
        _isStreaming.value = false
      }
    }
  }

  fun toggleTerminal() {
    _terminalVisible.value = !_terminalVisible.value
  }
}
