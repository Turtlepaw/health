package com.turtlepaw.shared.theming

import androidx.compose.ui.graphics.Color
import com.turtlepaw.shared.database.services.ServiceType

val PrimaryServiceColors = mapOf(
    ServiceType.Sunlight to ServiceColors(
        primary = SunlightColors.primary,
        trackColor = ThemeAwareColor(
            light = Color(0xFFF2E6D9),
            dark = Color(0xFF3A342B)
        )
    ),
    ServiceType.Sleep to ServiceColors(
        primary = SleepColors.primary,
        trackColor = ThemeAwareColor(
            SleepColors.secondary,
            SleepColors.secondary
        )
    )
)

class ThemeAwareColor(
    val light: Color,
    val dark: Color
)

class ServiceColors(
    val primary: Color,
    val trackColor: ThemeAwareColor
)

class Colors(
    val primary: Color,
    val secondary: Color,
    val onSecondary: Color? = null,
    val onPrimary: Color? = null,
)

fun Colors.toMaterialColors(): androidx.wear.compose.material.Colors {
    return androidx.wear.compose.material.Colors(
        primary = primary,
        secondary = secondary,
        onPrimary = onPrimary ?: Color(0xFF303133),
        onSecondary = onSecondary ?: Color(0xFF303133),
    )
}

fun Colors.toMaterial3Colors(): androidx.wear.compose.material3.ColorScheme {
    return androidx.wear.compose.material3.ColorScheme(
        primary = primary,
        secondary = secondary,
        onPrimary = onPrimary ?: Color(0xFF303133),
        onSecondary = onSecondary ?: Color(0xFF303133),
    )
}