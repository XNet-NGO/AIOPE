package com.aiope2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.aiope2.core.designsystem.theme.AiopeTheme
import com.aiope2.core.navigation.AppComposeNavigator
import com.aiope2.navigation.AiopeNavHost

@Composable
fun AiopeMain(composeNavigator: AppComposeNavigator) {
  AiopeTheme {
    val navHostController = rememberNavController()
    LaunchedEffect(Unit) {
      composeNavigator.handleNavigationCommands(navHostController)
    }
    AiopeNavHost(navHostController = navHostController, composeNavigator = composeNavigator)
  }
}
