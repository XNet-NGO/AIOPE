package com.aiope2.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aiope2.core.navigation.AiopeScreens
import com.aiope2.core.navigation.AppComposeNavigator
import com.aiope2.feature.chat.ChatScreen

fun NavGraphBuilder.aiopeNavigation(composeNavigator: AppComposeNavigator) {
  composable(route = AiopeScreens.Chat.route) {
    ChatScreen()
  }
  composable(route = AiopeScreens.Settings.route) {
    // TODO: SettingsScreen
  }
}
