package com.aiope2.core.navigation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationCommandTest {

  @Test
  fun `NavigateUp is a NavigationCommand`() {
    val cmd: NavigationCommand = NavigationCommand.NavigateUp
    assertTrue(cmd is NavigationCommand)
  }

  @Test
  fun `NavigateUp is a singleton — same reference every time`() {
    assertSame(NavigationCommand.NavigateUp, NavigationCommand.NavigateUp)
  }

  @Test
  fun `NavigateUp instance is non-null`() {
    assertNotNull(NavigationCommand.NavigateUp)
  }
}
