package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSurface,
    tertiary = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    secondaryContainer = CrimsonRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CrimsonRed,
    secondary = CharcoalDark,
    tertiary = AccentBlack,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = AccentBlack,
    onSurface = AccentBlack,
    secondaryContainer = CrimsonRedLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force false to maintain premium Crimson Red brand colors as requested by the user
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
