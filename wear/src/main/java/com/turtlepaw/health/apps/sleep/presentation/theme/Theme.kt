package com.turtlepaw.sleeptools.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val PRIMARY_COLOR = Color(0xFFE4C6FF)

@Composable
fun SleepTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        content = content,
        colors = Colors(
            primary = PRIMARY_COLOR,
            secondary = Color(android.graphics.Color.parseColor("#303333")),
            onSecondary = Color(android.graphics.Color.parseColor("#e0e1e1")),
            background = Color.Black
        )
    )
}