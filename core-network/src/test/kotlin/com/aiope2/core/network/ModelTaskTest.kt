package com.aiope2.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTaskTest {

  @Test
  fun `configurable tasks do not include CHAT`() {
    assertFalse(ModelTask.configurable.contains(ModelTask.CHAT))
  }

  @Test
  fun `configurable tasks includes all non-CHAT tasks`() {
    val allExceptChat = ModelTask.entries.filter { it != ModelTask.CHAT }
    assertEquals(allExceptChat, ModelTask.configurable)
  }

  @Test
  fun `all task ids are unique`() {
    val ids = ModelTask.entries.map { it.id }
    assertEquals(ids.size, ids.toSet().size)
  }

  @Test
  fun `all tasks have non-blank label and description`() {
    for (task in ModelTask.entries) {
      assertTrue("Task ${task.name} label should not be blank", task.label.isNotBlank())
      assertTrue("Task ${task.name} description should not be blank", task.description.isNotBlank())
    }
  }

  @Test
  fun `CHAT task has expected id`() {
    assertEquals("chat", ModelTask.CHAT.id)
  }

  @Test
  fun `SUMMARY task has expected id`() {
    assertEquals("summary", ModelTask.SUMMARY.id)
  }

  @Test
  fun `TITLE task has expected id`() {
    assertEquals("title", ModelTask.TITLE.id)
  }

  @Test
  fun `TRANSLATION task has expected id`() {
    assertEquals("translation", ModelTask.TRANSLATION.id)
  }

  @Test
  fun `TaskModelConfig defaults to null profileId and modelId`() {
    val config = TaskModelConfig(taskId = "chat")
    assertEquals("chat", config.taskId)
    assertNull(config.profileId)
    assertNull(config.modelId)
  }

  @Test
  fun `TaskModelConfig stores provided values`() {
    val config = TaskModelConfig(taskId = "summary", profileId = "prof-1", modelId = "gpt-4o")
    assertEquals("summary", config.taskId)
    assertEquals("prof-1", config.profileId)
    assertEquals("gpt-4o", config.modelId)
  }

  @Test
  fun `two TaskModelConfigs with same fields are equal`() {
    val c1 = TaskModelConfig("title", "p1", "model-a")
    val c2 = TaskModelConfig("title", "p1", "model-a")
    assertEquals(c1, c2)
  }

  @Test
  fun `two TaskModelConfigs with different taskId are not equal`() {
    val c1 = TaskModelConfig("title")
    val c2 = TaskModelConfig("summary")
    assertNotEquals(c1, c2)
  }

  @Test
  fun `ModelTask entries count matches expected`() {
    // 7 tasks defined in the enum
    assertEquals(7, ModelTask.entries.size)
  }
}
