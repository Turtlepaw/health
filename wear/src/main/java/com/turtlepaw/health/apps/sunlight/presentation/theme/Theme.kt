package com.turtlepaw.health.apps.sunlight.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import com.turtlepaw.shared.theming.SunlightColors

@Composable
fun SunlightTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        content = content,
        colorScheme = SunlightColors.colorScheme
    )
}