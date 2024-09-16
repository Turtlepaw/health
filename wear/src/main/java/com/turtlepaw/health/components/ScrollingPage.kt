package com.turtlepaw.health.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.scrollAway

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Page(
    rotaryMode: ScalingLazyColumnState.RotaryMode = ScalingLazyColumnState.RotaryMode.Scroll,
    showTimeText: Boolean = true,
    applyPadding: Boolean = true,
    content: ScalingLazyListScope.() -> Unit
) {
    val columnState = rememberColumnState(
        ScalingLazyColumnDefaults.responsive(
            rotaryMode = rotaryMode,
            hapticsEnabled = true,
            verticalArrangement = Arrangement.spacedBy(
                space = if (applyPadding) 4.dp else 0.dp,
                alignment = Alignment.Top
            )
        ),
    )

    Scaffold(timeText = if (showTimeText) {
        { ResponsiveTimeText(modifier = Modifier.scrollAway(columnState)) }
    } else null, positionIndicator = {
        PositionIndicator(columnState.state)
    }, vignette = {
        Vignette(vignettePosition = VignettePosition.TopAndBottom)
    }) {
        ScalingLazyColumn(
            columnState = columnState, content = content
        )
    }
}