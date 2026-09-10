package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Color Scheme (Preserves TrackEdu identity)
private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = DeepBlue,
    secondary = TealDark,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = PrimaryBlue,
    onTertiary = PureWhite,
    tertiaryContainer = Color(0xFFE0E7FF),
    onTertiaryContainer = DeepBlue,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFCBD5E1),
    outline = LightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = CriticalRed,
    onError = PureWhite,
    errorContainer = AttendanceAbsentContainer,
    onErrorContainer = AttendanceAbsentOnContainer
)

// Dark Color Scheme (Matches master dashboard visual reference)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = PureWhite,
    primaryContainer = DeepBlue,
    onPrimaryContainer = Color(0xFFDCE1FF),
    secondary = BrightCyan,
    onSecondary = NavyBackground,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = TealAccent,
    onTertiary = PureWhite,
    tertiaryContainer = Color(0xFF0F766E),
    onTertiaryContainer = Color(0xFFCCFBF1),
    background = NavyBackground,
    onBackground = TextPrimaryDark,
    surface = NavyCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = NavySecondary,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = NavyBackground,
    surfaceContainerLow = NavySecondary,
    surfaceContainer = NavyCard,
    surfaceContainerHigh = NavyCardElevated,
    surfaceContainerHighest = Color(0xFF263859),
    outline = NavyBorder,
    outlineVariant = Color(0xFF334155),
    error = CriticalRed,
    onError = PureWhite,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFCA5A5)
)

@Composable
fun TrackEduTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

