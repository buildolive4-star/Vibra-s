package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VibraDarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = SoftWhite,
    primaryContainer = DeepSurface,
    onPrimaryContainer = SoftWhite,
    secondary = NeonPink,
    onSecondary = SoftWhite,
    secondaryContainer = ElevatedGlass,
    onSecondaryContainer = SoftWhite,
    tertiary = RoyalBlue,
    onTertiary = SoftWhite,
    tertiaryContainer = GoldenGlow,
    onTertiaryContainer = DarkNavy,
    background = DarkNavy,
    onBackground = SoftWhite,
    surface = MidnightPurple,
    onSurface = SoftWhite,
    surfaceVariant = DeepSurface,
    onSurfaceVariant = MutedLavender,
    outline = GlassBorder,
    outlineVariant = SubtleSlate,
    error = CrimsonAlert,
    onError = SoftWhite
)

val VibraShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun VibraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VibraDarkColorScheme,
        typography = VibraTypography,
        shapes = VibraShapes,
        content = content
    )
}
