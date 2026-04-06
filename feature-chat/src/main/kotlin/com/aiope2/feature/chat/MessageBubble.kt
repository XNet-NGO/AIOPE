package com.aiope2.feature.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun MessageBubble(
  message: ChatMessage,
  onEdit: (() -> Unit)? = null,
  onRetry: (() -> Unit)? = null,
  onCopy: (() -> Unit)? = null
) {
  val isUser = message.role == Role.USER
  val alignment = if (isUser) Arrangement.End else Arrangement.Start
  val bgColor = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
  val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurface
  val textColorArgb = textColor.toArgb()
  val ctx = LocalContext.current
  val markwon = remember { Markwon.create(ctx) }
  var showMenu by remember { mutableStateOf(false) }

  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = alignment) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = bgColor,
      modifier = Modifier.widthIn(max = 320.dp)
    ) {
      Column {
        if (isUser) {
          SelectionContainer {
            Text(
              text = message.content,
              color = textColor,
              modifier = Modifier.padding(12.dp),
              style = MaterialTheme.typography.bodyMedium
            )
          }
        } else {
          AndroidView(
            factory = { context ->
              TextView(context).apply {
                setTextColor(textColorArgb)
                textSize = 14f
                setTextIsSelectable(true)
                setPadding(32, 24, 32, 8)
              }
            },
            update = { tv -> markwon.setMarkdown(tv, message.content) },
            modifier = Modifier.fillMaxWidth()
          )
        }

        // ··· menu button
        Box(Modifier.fillMaxWidth().padding(end = 4.dp, bottom = 2.dp), contentAlignment = Alignment.CenterEnd) {
          IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.MoreVert, "More", modifier = Modifier.size(14.dp),
              tint = textColor.copy(alpha = 0.5f))
          }
          DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Copy") }, onClick = {
              showMenu = false
              val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              cm.setPrimaryClip(ClipData.newPlainText("message", message.content))
              Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
            })
            if (isUser && onEdit != null) {
              DropdownMenuItem(text = { Text("Edit & Resend") }, onClick = { showMenu = false; onEdit() })
            }
            if (!isUser && onRetry != null) {
              DropdownMenuItem(text = { Text("Retry") }, onClick = { showMenu = false; onRetry() })
            }
          }
        }
      }
    }
  }
}
