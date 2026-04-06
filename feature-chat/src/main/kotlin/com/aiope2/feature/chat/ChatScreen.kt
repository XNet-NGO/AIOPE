package com.aiope2.feature.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
  val terminalVisible by viewModel.terminalVisible.collectAsStateWithLifecycle()
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp

  // Detect keyboard visibility
  val view = LocalView.current
  var keyboardVisible by remember { mutableStateOf(false) }
  DisposableEffect(view) {
    val listener = ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
      keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
      insets
    }
    onDispose { ViewCompat.setOnApplyWindowInsetsListener(view, null) }
  }

  if (isLandscape) {
    LandscapeLayout(messages, isStreaming, terminalVisible, keyboardVisible, viewModel::send, viewModel::toggleTerminal)
  } else {
    PortraitLayout(messages, isStreaming, terminalVisible, keyboardVisible, viewModel::send, viewModel::toggleTerminal)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitLayout(
  messages: List<ChatMessage>,
  isStreaming: Boolean,
  terminalVisible: Boolean,
  keyboardVisible: Boolean,
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .imePadding()
    ) {
      // Chat messages take remaining space
      MessageList(messages = messages, modifier = Modifier.weight(1f))

      // Input bar
      ChatInput(onSend = onSend, isStreaming = isStreaming)

      // Terminal panel (fixed height)
      if (terminalVisible) {
        TerminalPanel(keyboardVisible = keyboardVisible, modifier = Modifier.fillMaxWidth().height(240.dp))
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
  keyboardVisible: Boolean,
  onSend: (String) -> Unit,
  onToggleTerminal: () -> Unit
) {
  Row(modifier = Modifier.fillMaxSize()) {
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
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .imePadding()
      ) {
        MessageList(messages = messages, modifier = Modifier.weight(1f))
        ChatInput(onSend = onSend, isStreaming = isStreaming)
      }
    }

    if (terminalVisible) {
      TerminalPanel(keyboardVisible = keyboardVisible, modifier = Modifier.width(360.dp).fillMaxHeight())
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
