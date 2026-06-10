package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF003354), // Dark deep blue contrast text for primary buttons
    primaryContainer = Slate600, // Primary action card background (#2D2F31)
    onPrimaryContainer = OnSurfaceLight,
    secondary = EmeraldGlow,
    onSecondary = Color(0xFF003354),
    tertiary = AmberAlert,
    background = Slate900, // #1A1C1E
    onBackground = OnSurfaceLight, // #E2E2E6
    surface = Slate800, // #232529 secondary grid-like card backgrounds
    onSurface = OnSurfaceLight,
    surfaceVariant = Slate700, // #44474E border outline coloring
    onSurfaceVariant = OnSurfaceMuted, // #C4C7CF
    error = CrimsonError,
    onError = Color.White
  )

private val LightColorScheme = DarkColorScheme // Keep it consistently premium dark

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for military-grade security-operation center look
  dynamicColor: Boolean = false, // Force consistent elegant branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
