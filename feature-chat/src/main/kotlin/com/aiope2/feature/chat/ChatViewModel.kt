package com.aiope2.feature.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aiope2.core.terminal.shell.ShellExecutor
import com.aiope2.feature.chat.db.ChatDao
import com.aiope2.feature.chat.db.ConversationEntity
import com.aiope2.feature.chat.db.MessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
  application: Application,
  private val chatDao: ChatDao
) : AndroidViewModel(application) {

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages = _messages.asStateFlow()

  private val _isStreaming = MutableStateFlow(false)
  val isStreaming = _isStreaming.asStateFlow()

  private val _terminalVisible = MutableStateFlow(false)
  val terminalVisible = _terminalVisible.asStateFlow()

  private var conversationId = UUID.randomUUID().toString()

  private val openai = OpenAI(OpenAIConfig(
    token = "unused",
    host = OpenAIHost("https://text.pollinations.ai/v1/")
  ))

  private val systemPrompt = """You are AIOPE, an AI coding assistant running on Android.
You have access to tools: run_sh (Android shell).
When the user asks you to run commands, use the run_sh tool.
Be concise and helpful."""

  init {
    viewModelScope.launch {
      chatDao.insertConversation(ConversationEntity(id = conversationId))
    }
  }

  fun send(text: String) {
    val userMsg = ChatMessage(role = Role.USER, content = text)
    _messages.value = _messages.value + userMsg

    viewModelScope.launch {
      chatDao.insertMessage(MessageEntity(
        id = userMsg.id, conversationId = conversationId,
        role = userMsg.role.value, content = userMsg.content
      ))

      _isStreaming.value = true
      try {
        val assistantMsg = ChatMessage(role = Role.ASSISTANT, content = "")
        _messages.value = _messages.value + assistantMsg

        val apiMessages = buildList {
          add(com.aallam.openai.api.chat.ChatMessage(role = ChatRole.System, content = systemPrompt))
          _messages.value.dropLast(1).forEach { msg ->
            add(com.aallam.openai.api.chat.ChatMessage(
              role = when (msg.role) {
                Role.USER -> ChatRole.User
                Role.ASSISTANT -> ChatRole.Assistant
                Role.SYSTEM -> ChatRole.System
                Role.TOOL -> ChatRole.Tool
              },
              content = msg.content
            ))
          }
        }

        val request = ChatCompletionRequest(
          model = ModelId("openai-fast"),
          messages = apiMessages
        )

        val sb = StringBuilder()
        openai.chatCompletions(request).collect { chunk ->
          chunk.choices.firstOrNull()?.delta?.content?.let { delta ->
            sb.append(delta)
            val updated = _messages.value.toMutableList()
            updated[updated.lastIndex] = updated.last().copy(content = sb.toString())
            _messages.value = updated
          }
        }

        // Persist final assistant message
        val finalMsg = _messages.value.last()
        chatDao.insertMessage(MessageEntity(
          id = finalMsg.id, conversationId = conversationId,
          role = finalMsg.role.value, content = finalMsg.content
        ))

        // Update conversation title from first user message
        if (_messages.value.size <= 2) {
          val title = text.take(50)
          chatDao.updateConversation(conversationId, title)
        }
      } catch (e: Exception) {
        val updated = _messages.value.toMutableList()
        updated[updated.lastIndex] = updated.last().copy(content = "Error: ${e.message}")
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
