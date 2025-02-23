package com.turtlepaw.shared.theming

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

object SleepColors {
    val primary = Color(0xFFDAB9F9)
    val onPrimary = Color(0xFF3E2459)
    val primaryContainer = Color(0xFF553B71)
    val onPrimaryContainer = Color(0xFFF0DBFF)
    val secondary = Color(0xFFD0C1DA)
    val onSecondary = Color(0xFF362C3F)
    val secondaryContainer = Color(0xFF4D4357)
    val onSecondaryContainer = Color(0xFFEDDDF6)
    val tertiary = Color(0xFFF3B7BE)
    val onTertiary = Color(0xFF4B252B)
    val tertiaryContainer = Color(0xFF653A41)
    val onTertiaryContainer = Color(0xFFFFD9DD)
    val error = Color(0xFFFFB4AB)
    val onError = Color(0xFF690005)
    val errorContainer = Color(0xFF93000A)
    val onErrorContainer = Color(0xFFFFDAD6)
    val background = Color(0xFF151218)
    val onBackground = Color(0xFFE8E0E8)
    val surface = Color(0xFF151218)
    val onSurface = Color(0xFFE8E0E8)
    val surfaceVariant = Color(0xFF4A454E)
    val onSurfaceVariant = Color(0xFFCCC4CE)
    val outline = Color(0xFF968E98)
    val outlineVariant = Color(0xFF4A454E)
    val scrim = Color(0xFF000000)
    val inverseSurface = Color(0xFFE8E0E8)
    val inverseOnSurface = Color(0xFF332F35)
    val inversePrimary = Color(0xFF6E528A)
    val surfaceDim = Color(0xFF151218)
    val surfaceBright = Color(0xFF3C383E)
    val surfaceContainerLowest = Color(0xFF100D12)
    val surfaceContainerLow = Color(0xFF1E1A20)
    val surfaceContainer = Color(0xFF221E24)
    val surfaceContainerHigh = Color(0xFF2C292E)
    val surfaceContainerHighest = Color(0xFF373339)

    val colorScheme = ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        onBackground = onBackground,
        onSurface = onSurface,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
    )
}