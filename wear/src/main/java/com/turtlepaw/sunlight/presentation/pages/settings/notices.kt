package com.turtlepaw.sunlight.presentation.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.sunlight.presentation.Routes
import com.turtlepaw.sunlight.presentation.components.ItemsListWithModifier
import com.turtlepaw.sunlight.presentation.theme.SleepTheme
import com.turtlepaw.sunlight.utils.Settings

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearNotices(){
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            TimeText(
                modifier = Modifier.scrollAway(scalingLazyListState)
            )
            PositionIndicator(
                scalingLazyListState = scalingLazyListState
            )
            ItemsListWithModifier(
                modifier = Modifier
                    .rotaryWithScroll(
                        reverseDirection = false,
                        focusRequester = focusRequester,
                        scrollableState = scalingLazyListState,
                    ),
                scrollableState = scalingLazyListState,
                verticalAlignment = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.Top,
                )
            ) {
                item {
                    Spacer(modifier = Modifier.padding(0.5.dp))
                }
                item {
                    Text(
                        text = "Warning",
                        style = MaterialTheme.typography.title3,
                        color = MaterialTheme.colors.error
                    )
                }
                item {
                    Text(
                        text = "Placing your watch in direct sunlight for long periods of time may damage your watch's battery life",
                        style = MaterialTheme.typography.body1
                    )
                }
                item {
                    Text(
                        text = "Notice",
                        style = MaterialTheme.typography.title3,
                        color = MaterialTheme.colors.primaryVariant
                    )
                }
                item {
                    Text(
                        text = "Your watch will stop tracking sunlight once you turn on Bedtime Mode to save battery, once deactivated, sunlight tracking will resume",
                        style = MaterialTheme.typography.body1
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(1.dp))
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun NoticesPreview() {
    WearSettings(
        navigate = {},
        goal = Settings.GOAL.getDefaultAsInt(),
        sunlightThreshold = Settings.SUN_THRESHOLD.getDefaultAsInt(),
        isBatterySaver = true,
        setBatterySaver = {}
    )
}