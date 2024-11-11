package com.turtlepaw.health

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun appColorScheme(): ColorScheme {
    val isDarkTheme = isSystemInDarkTheme()
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val lightColorScheme = lightColorScheme(primary = Color(0xFF1EB980))
    val darkColorScheme = darkColorScheme(primary = Color(0xFF66ffc7))

    return when {
        supportsDynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }

        supportsDynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }

        isDarkTheme -> darkColorScheme
        else -> lightColorScheme
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = appColorScheme(),
        content = content
    )
}