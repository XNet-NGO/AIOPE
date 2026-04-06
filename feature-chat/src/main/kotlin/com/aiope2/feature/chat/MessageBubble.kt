package com.aiope2.feature.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessageBubble(message: ChatMessage) {
  val isUser = message.role == Role.USER
  val alignment = if (isUser) Arrangement.End else Arrangement.Start
  val bgColor = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
  val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurface

  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = alignment) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = bgColor,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Text(
        text = message.content,
        color = textColor,
        modifier = Modifier.padding(12.dp),
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}
