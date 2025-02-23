package com.turtlepaw.health.apps.sunlight.presentation.pages

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.sunlight.presentation.GoalCompleteActivity
import com.turtlepaw.health.services.LightLoggerService
import com.turtlepaw.health.services.LightWorker
import com.turtlepaw.health.services.SensorReceiver
import com.turtlepaw.shared.components.Material3Page
import com.turtlepaw.shared.database.sunlight.SunlightDay


@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun ClockworkToolkit(
    light: Float,
    context: Context,
    history: List<SunlightDay>
) {
    val sensorWorker = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, SensorReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
    )
//        val lightWorker = PendingIntent.getBroadcast(
//            context,
//            0,
//            Intent(context, LightLoggerService::class.java),
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
//        )
    val lightWorker = isServiceRunning(LightWorker::class.java, context)
    val isSampling = isServiceRunning(LightLoggerService::class.java, context)
    Material3Page {
        item {
            Text(
                text = "Toolkit",
                style = MaterialTheme.typography.titleSmall
            )
        }
        item {
            Text(
                text = "Debug tools for developers",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(modifier = Modifier.padding(2.dp))
        }

        item {
            Card(
                onClick = { /*TODO*/ },
                enabled = false,
            ) {
                Text(
                    text = "Current Light",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = "$light lx", fontWeight = FontWeight.Medium)
            }
        }

        item {
            Card(
                onClick = { /*TODO*/ },
                enabled = false,
            ) {
                Text(
                    text = "Light Worker",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (lightWorker) "Running"
                    else if (sensorWorker != null) "Idle"
                    else if (isSampling) "Sampling"
                    else "Not running",
                    color = if (lightWorker) MaterialTheme.colorScheme.primary
                    else if (sensorWorker != null) Color.Yellow
                    else if (isSampling) Color.Blue
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            Card(
                onClick = {
                    val intent = Intent(context, GoalCompleteActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                },
                //enabled = false,
            ) {
                Text(
                    text = "Simulate Goal Complete",
                    fontWeight = FontWeight.Medium
                )
            }
        }
        item {
            Text(
                text = "Running Tropical Bunny",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

@SuppressWarnings("deprecation")
fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
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
        light = 2000f,
        context = LocalContext.current,
        history = emptyList()
    )
}