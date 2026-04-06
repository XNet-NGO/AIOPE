package com.aiope2.feature.chat

data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val role: Role = Role.USER,
  val content: String = "",
  val timestamp: Long = System.currentTimeMillis()
)

enum class Role { USER, ASSISTANT, SYSTEM, TOOL }
