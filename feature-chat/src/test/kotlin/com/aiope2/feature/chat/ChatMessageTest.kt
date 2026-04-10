package com.aiope2.feature.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMessageTest {

  @Test
  fun `default ChatMessage role is USER`() {
    val msg = ChatMessage()
    assertEquals(Role.USER, msg.role)
  }

  @Test
  fun `default ChatMessage id is not blank`() {
    val msg = ChatMessage()
    assertTrue(msg.id.isNotBlank())
  }

  @Test
  fun `two default ChatMessages have different ids`() {
    val msg1 = ChatMessage()
    val msg2 = ChatMessage()
    assertNotEquals(msg1.id, msg2.id)
  }

  @Test
  fun `default ChatMessage content is empty`() {
    val msg = ChatMessage()
    assertEquals("", msg.content)
  }

  @Test
  fun `default ChatMessage reasoning is empty list`() {
    val msg = ChatMessage()
    assertTrue(msg.reasoning.isEmpty())
  }

  @Test
  fun `default ChatMessage isReasoningDone is true`() {
    val msg = ChatMessage()
    assertTrue(msg.isReasoningDone)
  }

  @Test
  fun `default ChatMessage toolCalls is empty`() {
    val msg = ChatMessage()
    assertTrue(msg.toolCalls.isEmpty())
  }

  @Test
  fun `default ChatMessage toolResults is empty`() {
    val msg = ChatMessage()
    assertTrue(msg.toolResults.isEmpty())
  }

  @Test
  fun `default ChatMessage imageUris is empty`() {
    val msg = ChatMessage()
    assertTrue(msg.imageUris.isEmpty())
  }

  @Test
  fun `default ChatMessage locationData is null`() {
    val msg = ChatMessage()
    assertNull(msg.locationData)
  }

  @Test
  fun `ChatMessage stores ASSISTANT role and content`() {
    val msg = ChatMessage(role = Role.ASSISTANT, content = "Hi there")
    assertEquals(Role.ASSISTANT, msg.role)
    assertEquals("Hi there", msg.content)
  }

  @Test
  fun `ChatMessage stores SYSTEM role`() {
    val msg = ChatMessage(role = Role.SYSTEM, content = "You are helpful.")
    assertEquals(Role.SYSTEM, msg.role)
  }

  @Test
  fun `ChatMessage stores TOOL role`() {
    val msg = ChatMessage(role = Role.TOOL, content = "{\"result\":\"ok\"}")
    assertEquals(Role.TOOL, msg.role)
  }

  @Test
  fun `ChatMessage stores reasoning list`() {
    val reasoning = listOf("Step 1: think", "Step 2: conclude")
    val msg = ChatMessage(reasoning = reasoning)
    assertEquals(2, msg.reasoning.size)
    assertEquals("Step 1: think", msg.reasoning[0])
  }

  @Test
  fun `ChatMessage stores imageUris`() {
    val uris = listOf("content://media/image1.jpg", "content://media/image2.jpg")
    val msg = ChatMessage(imageUris = uris)
    assertEquals(2, msg.imageUris.size)
    assertEquals("content://media/image1.jpg", msg.imageUris[0])
  }

  @Test
  fun `ChatMessage stores locationData`() {
    val loc = LocationData(latitude = 51.5074, longitude = -0.1278)
    val msg = ChatMessage(role = Role.USER, content = "Where am I?", locationData = loc)
    assertNotNull(msg.locationData)
    assertEquals(51.5074, msg.locationData!!.latitude, 0.0001)
    assertEquals(-0.1278, msg.locationData!!.longitude, 0.0001)
  }

  @Test
  fun `Role_from returns correct role for valid values`() {
    assertEquals(Role.USER, Role.from("user"))
    assertEquals(Role.ASSISTANT, Role.from("assistant"))
    assertEquals(Role.SYSTEM, Role.from("system"))
    assertEquals(Role.TOOL, Role.from("tool"))
  }

  @Test
  fun `Role_from returns USER for unknown string`() {
    assertEquals(Role.USER, Role.from("unknown_role"))
  }

  @Test
  fun `Role_from returns USER for empty string`() {
    assertEquals(Role.USER, Role.from(""))
  }

  @Test
  fun `Role values match expected strings`() {
    assertEquals("user", Role.USER.value)
    assertEquals("assistant", Role.ASSISTANT.value)
    assertEquals("system", Role.SYSTEM.value)
    assertEquals("tool", Role.TOOL.value)
  }

  @Test
  fun `LocationData stores latitude and longitude`() {
    val loc = LocationData(latitude = 37.7749, longitude = -122.4194)
    assertEquals(37.7749, loc.latitude, 0.0001)
    assertEquals(-122.4194, loc.longitude, 0.0001)
    assertNull(loc.altitude)
    assertNull(loc.speed)
    assertNull(loc.bearing)
    assertNull(loc.accuracy)
  }

  @Test
  fun `LocationData stores all optional fields`() {
    val loc = LocationData(
      latitude = 37.7749,
      longitude = -122.4194,
      altitude = 10.0,
      speed = 5.5,
      bearing = 90.0,
      accuracy = 3.0,
    )
    assertEquals(10.0, loc.altitude)
    assertEquals(5.5, loc.speed)
    assertEquals(90.0, loc.bearing)
    assertEquals(3.0, loc.accuracy)
  }

  @Test
  fun `ChatMessage copy with modified role creates new instance`() {
    val original = ChatMessage(role = Role.USER, content = "Hello")
    val copy = original.copy(role = Role.ASSISTANT)
    assertEquals(Role.ASSISTANT, copy.role)
    assertEquals("Hello", copy.content)
    assertEquals(original.id, copy.id)
  }

  @Test
  fun `ChatMessage with explicit id stores that id`() {
    val msg = ChatMessage(id = "fixed-id-123")
    assertEquals("fixed-id-123", msg.id)
  }
}
