package com.aiope2.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AiopeScreensTest {

  @Test
  fun `Chat screen has route 'chat'`() {
    assertEquals("chat", AiopeScreens.Chat.route)
  }

  @Test
  fun `Settings screen has route 'settings'`() {
    assertEquals("settings", AiopeScreens.Settings.route)
  }

  @Test
  fun `Chat and Settings screens have different routes`() {
    assertNotEquals(AiopeScreens.Chat.route, AiopeScreens.Settings.route)
  }

  @Test
  fun `Chat screen route is non-blank`() {
    assert(AiopeScreens.Chat.route.isNotBlank())
  }

  @Test
  fun `Settings screen route is non-blank`() {
    assert(AiopeScreens.Settings.route.isNotBlank())
  }

  @Test
  fun `Chat is an AiopeScreens instance`() {
    assert(AiopeScreens.Chat is AiopeScreens)
  }

  @Test
  fun `Settings is an AiopeScreens instance`() {
    assert(AiopeScreens.Settings is AiopeScreens)
  }
}
