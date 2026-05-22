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

private val DarkColorScheme = darkColorScheme(
  primary = NeonGreen,
  secondary = NeonAqua,
  tertiary = GlowYellow,
  background = MatteBlack,
  surface = DarkGrey,
  surfaceVariant = MediumGrey,
  onPrimary = MatteBlack,
  onSecondary = MatteBlack,
  onTertiary = MatteBlack,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  onSurfaceVariant = TextSecondary,
  outline = BorderGrey
)

private val LightColorScheme = DarkColorScheme // Standard black-and-white theme is dark by design!

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark mode for premium theme consistency
  dynamicColor: Boolean = false, // Use our handcrafted monochrome theme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
