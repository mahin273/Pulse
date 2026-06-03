package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = Primary,
  secondary = Secondary,
  background = Background,
  surface = Surface,
  onPrimary = Background,
  onSecondary = Background,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceVariant,
  onSurfaceVariant = TextSecondary,
  error = Error
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for AMOLED root optimizer style
  dynamicColor: Boolean = false, // Force custom cyan/purple brand colors
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
