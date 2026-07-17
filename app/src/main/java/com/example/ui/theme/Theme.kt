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
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = androidx.compose.ui.graphics.Color(0xFF13151A),
    surface = androidx.compose.ui.graphics.Color(0xFF1F1D23),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE6E1E5)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = androidx.compose.ui.graphics.Color(0xFFFEF7FF),
    surface = androidx.compose.ui.graphics.Color(0xFFFEF7FF),
    onBackground = androidx.compose.ui.graphics.Color(0xFF1D1B20),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1D1B20)
  )

private val GreenDarkColorScheme =
  darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenPrimaryContainerDark,
    onPrimaryContainer = GreenOnPrimaryContainerDark,
    secondary = GreenSecondaryDark,
    onSecondary = GreenOnSecondaryDark,
    secondaryContainer = GreenSecondaryContainerDark,
    onSecondaryContainer = GreenOnSecondaryContainerDark,
    tertiary = GreenTertiaryDark,
    onTertiary = GreenOnTertiaryDark,
    tertiaryContainer = GreenTertiaryContainerDark,
    onTertiaryContainer = GreenOnTertiaryContainerDark,
    background = GreenBackgroundDark,
    onBackground = GreenOnBackgroundDark,
    surface = GreenSurfaceDark,
    onSurface = GreenOnSurfaceDark,
    surfaceVariant = GreenSurfaceVariantDark,
    onSurfaceVariant = GreenOnSurfaceVariantDark,
    outline = GreenOutlineDark
  )

private val GreenLightColorScheme =
  lightColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = GreenOnPrimaryLight,
    primaryContainer = GreenPrimaryContainerLight,
    onPrimaryContainer = GreenOnPrimaryContainerLight,
    secondary = GreenSecondaryLight,
    onSecondary = GreenOnSecondaryLight,
    secondaryContainer = GreenSecondaryContainerLight,
    onSecondaryContainer = GreenOnSecondaryContainerLight,
    tertiary = GreenTertiaryLight,
    onTertiary = GreenOnTertiaryLight,
    tertiaryContainer = GreenTertiaryContainerLight,
    onTertiaryContainer = GreenOnTertiaryContainerLight,
    background = GreenBackgroundLight,
    onBackground = GreenOnBackgroundLight,
    surface = GreenSurfaceLight,
    onSurface = GreenOnSurfaceLight,
    surfaceVariant = GreenSurfaceVariantLight,
    onSurfaceVariant = GreenOnSurfaceVariantLight,
    outline = GreenOutlineLight
  )

private val MonoLightColorScheme =
  lightColorScheme(
    primary = MonoPrimaryLight,
    onPrimary = MonoOnPrimaryLight,
    primaryContainer = MonoPrimaryContainerLight,
    onPrimaryContainer = MonoOnPrimaryContainerLight,
    secondary = MonoSecondaryLight,
    onSecondary = MonoOnSecondaryLight,
    secondaryContainer = MonoSecondaryContainerLight,
    onSecondaryContainer = MonoOnSecondaryContainerLight,
    tertiary = MonoTertiaryLight,
    onTertiary = MonoOnTertiaryLight,
    tertiaryContainer = MonoTertiaryContainerLight,
    onTertiaryContainer = MonoOnTertiaryContainerLight,
    background = MonoBackgroundLight,
    onBackground = MonoOnBackgroundLight,
    surface = MonoSurfaceLight,
    onSurface = MonoOnSurfaceLight,
    surfaceVariant = MonoSurfaceVariantLight,
    onSurfaceVariant = MonoOnSurfaceVariantLight,
    outline = MonoOutlineLight
  )

private val MonoDarkColorScheme =
  darkColorScheme(
    primary = MonoPrimaryDark,
    onPrimary = MonoOnPrimaryDark,
    primaryContainer = MonoPrimaryContainerDark,
    onPrimaryContainer = MonoOnPrimaryContainerDark,
    secondary = MonoSecondaryDark,
    onSecondary = MonoOnSecondaryDark,
    secondaryContainer = MonoSecondaryContainerDark,
    onSecondaryContainer = MonoOnSecondaryContainerDark,
    tertiary = MonoTertiaryDark,
    onTertiary = MonoOnTertiaryDark,
    tertiaryContainer = MonoTertiaryContainerDark,
    onTertiaryContainer = MonoOnTertiaryContainerDark,
    background = MonoBackgroundDark,
    onBackground = MonoOnBackgroundDark,
    surface = MonoSurfaceDark,
    onSurface = MonoOnSurfaceDark,
    surfaceVariant = MonoSurfaceVariantDark,
    onSurfaceVariant = MonoOnSurfaceVariantDark,
    outline = MonoOutlineDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  pitchBlack: Boolean = false,
  content: @Composable () -> Unit,
) {
  val baseScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> MonoDarkColorScheme
      else -> MonoLightColorScheme
    }

  val colorScheme = if (darkTheme && pitchBlack) {
    baseScheme.copy(
      background = androidx.compose.ui.graphics.Color(0xFF000000),
      surface = androidx.compose.ui.graphics.Color(0xFF000000),
      surfaceVariant = androidx.compose.ui.graphics.Color(0xFF121212),
      onBackground = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
      onSurface = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    )
  } else {
    baseScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
