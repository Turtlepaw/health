package com.turtlepaw.shared.theming

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

object SunlightColors {
    val primary = Color(0xFFEEBF6D)
    val onPrimary = Color(0xFF422C00)
    val primaryContainer = Color(0xFF5E4100)
    val onPrimaryContainer = Color(0xFFFFDEA9)
    val secondary = Color(0xFFDAC3A1)
    val onSecondary = Color(0xFF3C2E16)
    val secondaryContainer = Color(0xFF54442A)
    val onSecondaryContainer = Color(0xFFF7DFBB)
    val tertiary = Color(0xFFB4CEA6)
    val onTertiary = Color(0xFF203619)
    val tertiaryContainer = Color(0xFF364D2E)
    val onTertiaryContainer = Color(0xFFCFEBC0)
    val error = Color(0xFFFFB4AB)
    val onError = Color(0xFF690005)
    val errorContainer = Color(0xFF93000A)
    val onErrorContainer = Color(0xFFFFDAD6)
    val background = Color(0xFF17130B)
    val onBackground = Color(0xFFECE1D4)
    val surface = Color(0xFF17130B)
    val onSurface = Color(0xFFECE1D4)
    val surfaceVariant = Color(0xFF4E4639)
    val onSurfaceVariant = Color(0xFFD1C5B4)
    val outline = Color(0xFF9A8F80)
    val outlineVariant = Color(0xFF4E4639)
    val scrim = Color(0xFF000000)
    val inverseSurface = Color(0xFFECE1D4)
    val inverseOnSurface = Color(0xFF353027)
    val inversePrimary = Color(0xFF7B580D)
    val surfaceDim = Color(0xFF17130B)
    val surfaceBright = Color(0xFF3E382F)
    val surfaceContainerLowest = Color(0xFF120E07)
    val surfaceContainerLow = Color(0xFF201B13)
    val surfaceContainer = Color(0xFF241F17)
    val surfaceContainerHigh = Color(0xFF2F2921)
    val surfaceContainerHighest = Color(0xFF3A342B)

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
        //bac= SunlightColors.background,
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