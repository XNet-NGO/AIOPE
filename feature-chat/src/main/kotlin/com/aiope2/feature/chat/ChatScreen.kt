package com.aiope2.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
  val terminalVisible by viewModel.terminalVisible.collectAsStateWithLifecycle()
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp

  if (isLandscape) {
    LandscapeLayout(
      messages = messages,
      isStreaming = isStreaming,
      terminalVisible = terminalVisible,
      onSend = viewModel::send,
      onToggleTerminal = viewModel::toggleTerminal
    )
  } else {
    PortraitLayout(
      messages = messages,
      isStreaming = isStreaming,
      terminalVisible = terminalVisible,
      onSend = viewModel::send,
      onToggleTerminal = viewModel::toggleTerminal
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitLayout(
  messages: List<ChatMessage>,
  isStreaming: Boolean,
  terminalVisible: Boolean,
  onSend: (String) -> Unit,
  onToggleTerminal: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("AIOPE") },
        actions = {
          IconButton(onClick = onToggleTerminal) {
            Icon(
              Icons.Default.Terminal,
              contentDescription = "Terminal",
              tint = if (terminalVisible) MaterialTheme.colorScheme.primary
                     else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      )
    }
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      // Chat messages
      val weight = if (terminalVisible) 0.6f else 1f
      MessageList(messages = messages, modifier = Modifier.weight(weight))

      // Input bar
      ChatInput(onSend = onSend, isStreaming = isStreaming)

      // Terminal panel (bottom)
      if (terminalVisible) {
        TerminalPlaceholder(modifier = Modifier.weight(0.4f))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeLayout(
  messages: List<ChatMessage>,
  isStreaming: Boolean,
  terminalVisible: Boolean,
  onSend: (String) -> Unit,
  onToggleTerminal: () -> Unit
) {
  Row(modifier = Modifier.fillMaxSize()) {
    // Chat side
    Scaffold(
      modifier = Modifier.weight(1f),
      topBar = {
        TopAppBar(
          title = { Text("AIOPE") },
          actions = {
            IconButton(onClick = onToggleTerminal) {
              Icon(
                Icons.Default.Terminal,
                contentDescription = "Terminal",
                tint = if (terminalVisible) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        )
      }
    ) { padding ->
      Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        MessageList(messages = messages, modifier = Modifier.weight(1f))
        ChatInput(onSend = onSend, isStreaming = isStreaming)
      }
    }

    // Terminal side (full height, no appbar)
    if (terminalVisible) {
      TerminalPlaceholder(modifier = Modifier.width(360.dp).fillMaxHeight())
    }
  }
}

@Composable
private fun MessageList(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) scope.launch { listState.animateScrollToItem(messages.size - 1) }
  }

  LazyColumn(state = listState, modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
    items(messages, key = { it.id }) { msg ->
      MessageBubble(message = msg)
      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@Composable
private fun ChatInput(onSend: (String) -> Unit, isStreaming: Boolean) {
  var text by remember { mutableStateOf("") }

  Row(
    modifier = Modifier.fillMaxWidth().padding(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    OutlinedTextField(
      value = text,
      onValueChange = { text = it },
      modifier = Modifier.weight(1f),
      placeholder = { Text("Message...") },
      maxLines = 4,
      enabled = !isStreaming
    )
    Spacer(modifier = Modifier.width(8.dp))
    IconButton(
      onClick = {
        if (text.isNotBlank()) {
          onSend(text.trim())
          text = ""
        }
      },
      enabled = text.isNotBlank() && !isStreaming
    ) {
      Icon(Icons.Default.Send, contentDescription = "Send")
    }
  }
}

@Composable
private fun TerminalPlaceholder(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp),
    contentAlignment = Alignment.Center
  ) {
    Text("Terminal (Termux TerminalView will go here)", style = MaterialTheme.typography.bodySmall)
  }
}
