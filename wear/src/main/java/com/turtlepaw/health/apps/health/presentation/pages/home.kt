package com.turtlepaw.health.apps.health.presentation.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.health.presentation.theme.HealthTheme
import com.turtlepaw.health.components.Page

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome() {
    HealthTheme {
        Page {
            item {
                Text(
                    text = "Health",
                    //modifier = Modifier.padding(bottom = 2.dp),
                    color = MaterialTheme.colors.onSurfaceVariant,
                    //fontWeight = FontWeight.W400
                )
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearHome()
}