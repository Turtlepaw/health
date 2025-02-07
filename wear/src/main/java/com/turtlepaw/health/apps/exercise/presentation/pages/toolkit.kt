package com.turtlepaw.heartconnect.presentation.pages

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.exercise.manager.ExerciseService
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import com.turtlepaw.shared.components.Page

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun ClockworkToolkit(
    noise: Double,
    context: Context
) {
    ExerciseTheme {
        val noiseWorker = isServiceRunning(ExerciseService::class.java, context)
        Page {
            item {
                Text(
                    text = "Toolkit",
                    style = MaterialTheme.typography.title3
                )
            }
            item {
                Text(
                    text = "Debug tools for developers",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
            item {
                Spacer(modifier = Modifier.padding(2.dp))
            }
            item {
                Card(
                    onClick = { /*TODO*/ },
                    backgroundPainter = CardDefaults.cardBackgroundPainter(
                        startBackgroundColor = MaterialTheme.colors.surface
                    ),
                ) {
                    Text(
                        text = "Current Noise",
                        style = MaterialTheme.typography.title3
                    )
                    Text(text = "${noise}dB", fontWeight = FontWeight.Medium)
                }
            }

            item {
                Card(
                    onClick = { /*TODO*/ },
                    backgroundPainter = CardDefaults.cardBackgroundPainter(
                        startBackgroundColor = MaterialTheme.colors.surface
                    )
                ) {
                    Text(
                        text = "Exercise Service",
                        style = MaterialTheme.typography.title3
                    )
                    Text(
                        text = if (noiseWorker) "Running"
                        else "Not running",
                        color = if (noiseWorker) MaterialTheme.colors.secondary
                        else MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@SuppressWarnings("deprecation")
private fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ToolkitPreview() {
    ClockworkToolkit(
        noise = 70.0,
        context = LocalContext.current
    )
}