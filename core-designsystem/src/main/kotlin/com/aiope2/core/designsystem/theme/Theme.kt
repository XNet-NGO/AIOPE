package com.aiope2.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
  primary = PRIMARY_DARK,
  background = BACKGROUND_DARK,
  surface = SURFACE_DARK,
  onSurface = ON_SURFACE_DARK
)

private val LightColorScheme = lightColorScheme(
  primary = PRIMARY,
  background = BACKGROUND_LIGHT,
  surface = SURFACE_LIGHT,
  onSurface = ON_SURFACE_LIGHT
)

private val LightBackgroundTheme = BackgroundTheme(color = BACKGROUND_LIGHT)
private val DarkBackgroundTheme = BackgroundTheme(color = BACKGROUND_DARK)

@Composable
fun AiopeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val backgroundTheme = if (darkTheme) DarkBackgroundTheme else LightBackgroundTheme

  CompositionLocalProvider(LocalBackgroundTheme provides backgroundTheme) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
