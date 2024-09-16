package com.turtlepaw.health.apps.sunlight.presentation.pages.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.components.Page

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearNotices(){
    Page {
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun NoticesPreview() {
    WearNotices()
}