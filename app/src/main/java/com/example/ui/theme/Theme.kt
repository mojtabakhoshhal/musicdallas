package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val DallasDarkColorScheme = darkColorScheme(
    primary = DallasGold,
    onPrimary = ObsidianBlack,
    primaryContainer = DallasGoldContainer,
    onPrimaryContainer = DallasGoldBright,
    secondary = DallasGoldBright,
    onSecondary = ObsidianBlack,
    secondaryContainer = ObsidianElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = OfflineEmerald,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianBlack,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianElevated,
    surfaceContainerHighest = ObsidianOutline,
    outline = ObsidianOutline,
    outlineVariant = ObsidianElevated,
    error = ErrorCoral,
    onError = ObsidianBlack,
)

private val DallasShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun DallasMusicTheme(
    layoutDirection: LayoutDirection = LayoutDirection.Rtl,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = DallasDarkColorScheme,
            typography = DallasTypography,
            shapes = DallasShapes,
            content = content,
        )
    }
}
