package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class BetZoneExtendedColors(
    val isDark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val electricBlue: Color,
    val neonGreen: Color,
    val goldenYellow: Color,
    val liveCrimson: Color,
    val subtleText: Color
)

val LocalBetZoneColors = staticCompositionLocalOf {
    BetZoneExtendedColors(
        isDark = true,
        bgTop = DeepBlack,
        bgBottom = DeepNavy,
        glassSurface = NavySurface,
        glassBorder = NavyBorder,
        electricBlue = ElectricBlue,
        neonGreen = NeonGreen,
        goldenYellow = GoldenYellow,
        liveCrimson = LiveCrimson,
        subtleText = MutedSlate
    )
}

private val DarkBetZoneScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = DeepBlack,
    primaryContainer = RoyalElectricBlue,
    onPrimaryContainer = SoftWhite,
    secondary = NeonGreen,
    onSecondary = DeepBlack,
    secondaryContainer = Color(0xFF0A3828),
    onSecondaryContainer = NeonGreen,
    tertiary = GoldenYellow,
    onTertiary = DeepBlack,
    tertiaryContainer = Color(0xFF3A2E04),
    onTertiaryContainer = GoldenYellow,
    background = DeepBlack,
    onBackground = SoftWhite,
    surface = DeepNavy,
    onSurface = SoftWhite,
    surfaceVariant = NavySurface,
    onSurfaceVariant = MutedSlate,
    surfaceContainerHigh = NavyCard,
    outline = NavyBorder,
    error = LiveCrimson,
    onError = SoftWhite
)

private val LightBetZoneScheme = lightColorScheme(
    primary = ElectricBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9ECFF),
    onPrimaryContainer = Color(0xFF002C66),
    secondary = NeonGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6F9E8),
    onSecondaryContainer = Color(0xFF003D1F),
    tertiary = GoldenAmberDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF1C2),
    onTertiaryContainer = Color(0xFF4D3500),
    background = LightBackground,
    onBackground = DarkInk,
    surface = LightSurface,
    onSurface = DarkInk,
    surfaceVariant = LightCard,
    onSurfaceVariant = MutedInk,
    surfaceContainerHigh = LightCard,
    outline = LightBorder,
    error = LiveCrimson,
    onError = Color.White
)

val BetZoneShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun BetZoneTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val extended = if (darkTheme) {
        BetZoneExtendedColors(
            isDark = true,
            bgTop = DeepBlack,
            bgBottom = DeepNavy,
            glassSurface = NavySurface,
            glassBorder = NavyBorder,
            electricBlue = ElectricBlue,
            neonGreen = NeonGreen,
            goldenYellow = GoldenYellow,
            liveCrimson = LiveCrimson,
            subtleText = MutedSlate
        )
    } else {
        BetZoneExtendedColors(
            isDark = false,
            bgTop = LightBackground,
            bgBottom = Color(0xFFE5EEFC),
            glassSurface = LightSurface,
            glassBorder = LightBorder,
            electricBlue = ElectricBlueDark,
            neonGreen = NeonGreenDark,
            goldenYellow = GoldenAmberDark,
            liveCrimson = LiveCrimson,
            subtleText = MutedInk
        )
    }

    CompositionLocalProvider(LocalBetZoneColors provides extended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkBetZoneScheme else LightBetZoneScheme,
            typography = BetZoneTypography,
            shapes = BetZoneShapes,
            content = content
        )
    }
}
