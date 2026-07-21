package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ImmersivePurplePrimary,
    onPrimary = ImmersivePurpleOnPrimary,
    secondary = ImmersiveIndigoAccent,
    tertiary = ImmersiveCyanAccent,
    background = ImmersiveBackground,
    onBackground = ImmersiveText,
    surface = ImmersiveSurface,
    onSurface = ImmersiveText,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = ImmersiveSubText,
    outline = ImmersiveOutline
  )

private val LightColorScheme = DarkColorScheme // Force dark theme everywhere for the "Immersive UI" aesthetic

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve Immersive UI theme colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
