package com.aiope2.feature.chat.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatStreamChunkTest {

  @Test
  fun `default ChatStreamChunk has empty content`() {
    val chunk = ChatStreamChunk()
    assertEquals("", chunk.content)
  }

  @Test
  fun `default ChatStreamChunk isDone is false`() {
    val chunk = ChatStreamChunk()
    assertFalse(chunk.isDone)
  }

  @Test
  fun `default ChatStreamChunk reasoning is null`() {
    val chunk = ChatStreamChunk()
    assertNull(chunk.reasoning)
  }

  @Test
  fun `default ChatStreamChunk toolCalls is null`() {
    val chunk = ChatStreamChunk()
    assertNull(chunk.toolCalls)
  }

  @Test
  fun `default ChatStreamChunk toolResults is null`() {
    val chunk = ChatStreamChunk()
    assertNull(chunk.toolResults)
  }

  @Test
  fun `default ChatStreamChunk error is null`() {
    val chunk = ChatStreamChunk()
    assertNull(chunk.error)
  }

  @Test
  fun `done chunk has isDone true`() {
    val chunk = ChatStreamChunk(isDone = true)
    assertTrue(chunk.isDone)
  }

  @Test
  fun `error chunk stores error message and isDone`() {
    val chunk = ChatStreamChunk(error = "Connection refused", isDone = true)
    assertEquals("Connection refused", chunk.error)
    assertTrue(chunk.isDone)
  }

  @Test
  fun `content chunk stores content`() {
    val chunk = ChatStreamChunk(content = "Hello, world!")
    assertEquals("Hello, world!", chunk.content)
  }

  @Test
  fun `reasoning chunk stores reasoning`() {
    val chunk = ChatStreamChunk(reasoning = "Let me think step by step...")
    assertEquals("Let me think step by step...", chunk.reasoning)
  }

  @Test
  fun `chunk with tool calls stores list`() {
    val toolCalls = listOf(
      ToolCallInfo(id = "call_1", name = "get_weather", arguments = mapOf("city" to "London")),
    )
    val chunk = ChatStreamChunk(toolCalls = toolCalls)
    assertNotNull(chunk.toolCalls)
    assertEquals(1, chunk.toolCalls!!.size)
    assertEquals("call_1", chunk.toolCalls!![0].id)
  }

  @Test
  fun `chunk with tool results stores list`() {
    val results = listOf(
      ToolResultInfo(id = "call_1", name = "get_weather", arguments = emptyMap(), result = "Sunny, 20°C"),
    )
    val chunk = ChatStreamChunk(toolResults = results)
    assertNotNull(chunk.toolResults)
    assertEquals(1, chunk.toolResults!!.size)
    assertEquals("Sunny, 20°C", chunk.toolResults!![0].result)
  }

  @Test
  fun `ChatStreamChunk equality holds for same field values`() {
    val chunk1 = ChatStreamChunk(content = "test", isDone = false)
    val chunk2 = ChatStreamChunk(content = "test", isDone = false)
    assertEquals(chunk1, chunk2)
  }

  @Test
  fun `ChatStreamChunk inequality when content differs`() {
    val chunk1 = ChatStreamChunk(content = "a")
    val chunk2 = ChatStreamChunk(content = "b")
    assertNotEquals(chunk1, chunk2)
  }

  @Test
  fun `ToolCallInfo stores id name and arguments`() {
    val tool = ToolCallInfo(
      id = "call_abc",
      name = "search_web",
      arguments = mapOf("query" to "Kotlin", "max_results" to 5),
    )
    assertEquals("call_abc", tool.id)
    assertEquals("search_web", tool.name)
    assertEquals("Kotlin", tool.arguments["query"])
    assertEquals(5, tool.arguments["max_results"])
  }

  @Test
  fun `ToolCallInfo with empty arguments`() {
    val tool = ToolCallInfo(id = "id", name = "no_params", arguments = emptyMap())
    assertTrue(tool.arguments.isEmpty())
  }

  @Test
  fun `ToolResultInfo stores id name arguments and result`() {
    val result = ToolResultInfo(
      id = "call_xyz",
      name = "run_code",
      arguments = mapOf("code" to "print('hi')"),
      result = "hi\n",
    )
    assertEquals("call_xyz", result.id)
    assertEquals("run_code", result.name)
    assertEquals("print('hi')", result.arguments["code"])
    assertEquals("hi\n", result.result)
  }

  @Test
  fun `ToolResultInfo equality`() {
    val r1 = ToolResultInfo("id", "tool", mapOf("k" to "v"), "ok")
    val r2 = ToolResultInfo("id", "tool", mapOf("k" to "v"), "ok")
    assertEquals(r1, r2)
  }

  @Test
  fun `multiple tool calls stored correctly`() {
    val calls = listOf(
      ToolCallInfo("c1", "tool_a", mapOf("a" to 1)),
      ToolCallInfo("c2", "tool_b", mapOf("b" to 2)),
    )
    val chunk = ChatStreamChunk(toolCalls = calls)
    assertEquals(2, chunk.toolCalls!!.size)
    assertEquals("tool_a", chunk.toolCalls!![0].name)
    assertEquals("tool_b", chunk.toolCalls!![1].name)
  }
}
