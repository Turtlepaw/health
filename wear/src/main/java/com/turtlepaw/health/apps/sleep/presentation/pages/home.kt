package com.turtlepaw.health.apps.sleep.presentation.pages

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.sleep.SleepTrackerHints
import com.turtlepaw.health.apps.sleep.SleepTrackerViewModel
import com.turtlepaw.health.apps.sleep.presentation.Routes
import com.turtlepaw.health.apps.sleep.presentation.theme.SleepTheme
import com.turtlepaw.shared.components.Material3Page
import com.turtlepaw.sleeptools.utils.AlarmType
import com.turtlepaw.sleeptools.utils.TimeManager
import kotlinx.coroutines.delay
import java.time.LocalTime

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome(
    navigate: (route: String) -> Unit,
    wakeTime: Pair<LocalTime, AlarmType>,
    nextAlarm: LocalTime,
    timeManager: TimeManager,
    bedtimeGoal: LocalTime?,
    viewModel: SleepTrackerViewModel = viewModel()
) {
    val isTracking by viewModel.isTracking.observeAsState()
    val isPaused by viewModel.isPaused.observeAsState()
    val isSleeping by viewModel.isSleeping.observeAsState()
    val hints by viewModel.hints.observeAsState()
    val context = LocalContext.current

    timeManager.getTimeFormatter(false)
    timeManager.getTimeFormatter()
    var timeDifference by remember {
        mutableStateOf(timeManager.calculateTimeDifference(wakeTime.first))
    }

    // Track the current minute
    var currentMinute by remember { mutableIntStateOf(LocalTime.now().minute) }
    // Track the sleep quality
    var sleepQuality by remember {
        mutableStateOf(timeManager.calculateSleepQuality(timeDifference))
    }

    // Use LaunchedEffect to launch a coroutine when the composable is first displayed
    LaunchedEffect(wakeTime) {
        val handler = Handler(Looper.getMainLooper())
        // Use a coroutine to run the code on the main thread
        while (true) {
            // Delay until the next minute
            delay(60_000 - (System.currentTimeMillis() % 60_000))

            // Update the current minute
            currentMinute = LocalTime.now().minute

            // Re-compose the composable
            handler.post {
                timeDifference = timeManager.calculateTimeDifference(nextAlarm)
                sleepQuality = timeManager.calculateSleepQuality(timeDifference)
                // we used to use wakeTime but now we use
                // nextAlarm
                // You can trigger a re-composition here, for example by updating some state
                // or forcing a re-layout of your composable
                // Uncomment the line below if your composable doesn't re-compose automatically
                // currentMinute++
            }
        }
    }

    Material3Page {
        item {
            Image(
                painter = painterResource(id = R.drawable.sleep),
                contentDescription = "sleep",
                modifier = Modifier
                    .size(32.dp)
                    .padding(bottom = 8.dp)
            )
        }
        item {
            Text(
                text = if (isTracking == true) {
                    if (isPaused == true) "Paused"
                    else if (isSleeping == true) "You're sleeping"
                    else "Tracking"
                } else "Not Tracking",
                color = Color(0xFFE4C6FF),
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
        if (hints?.isNotEmpty() == true) {
            item {
                Spacer(modifier = Modifier.padding(vertical = 3.dp))
            }
        }
        items((hints ?: emptyMap()).toList()) {
            AppCard(
                onClick = {},
                enabled = false,
                title = {},
                appName = {
                    Text(
                        text = when (it.first) {
                            SleepTrackerHints.MotionLow -> "Low Motion"
                            SleepTrackerHints.HeartRateLow -> "Low Heart Rate"
                            SleepTrackerHints.LightSleepDetected -> "Light Sleep"
                        }
                    )
                },
                appImage = {
                    Icon(
                        painterResource(
                            id = when (it.first) {
                                SleepTrackerHints.MotionLow -> R.drawable.ic_waves
                                SleepTrackerHints.HeartRateLow -> R.drawable.ic_vital_signs
                                SleepTrackerHints.LightSleepDetected -> R.drawable.ic_hotel
                            }
                        ),
                        contentDescription = when (it.first) {
                            SleepTrackerHints.MotionLow -> "Waves"
                            SleepTrackerHints.HeartRateLow -> "Vital Signs"
                            SleepTrackerHints.LightSleepDetected -> "Bed"
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(18.dp)
                    )
                },

                ) {
                Text(
                    text = when (it.first) {
                        SleepTrackerHints.MotionLow -> "You haven't moved for a while. Your average motion is ${it.second}."
                        SleepTrackerHints.HeartRateLow -> "Your heart rate is low. Your heart rate was ${it.second}."
                        SleepTrackerHints.LightSleepDetected -> "Light sleep detected."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
//        item {
//            Text(
//                text = "${sleepQuality.getTitle()} • ${nextAlarm.format(formatter)} wake up${if (wakeTime.second === AlarmType.SYSTEM_ALARM) " (alarm)" else ""}",
//                modifier = Modifier.padding(top = 4.dp)
//            )
//        }

        item {
            Spacer(modifier = Modifier.padding(vertical = 3.dp))
        }

        item {
            Button(
                onClick = {
                    if (isTracking == true) {
                        viewModel.stopTracking(context)
                    } else {
                        viewModel.startTracking(context)
                        viewModel.bindService()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = "${if (isTracking == true) "Stop" else "Start"} Tracking",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                icon = {
                    Icon(
                        if (isTracking == true) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = "${if (isTracking == true) "Stop" else "Start"} Tracking",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                IconButton(
                    onClick = {
                        navigate(
                            Routes.HISTORY.getRoute()
                        )
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                ) {
                    // Icon for history button
                    Icon(
                        painter = painterResource(id = R.drawable.history),
                        contentDescription = "History",
                        tint = Color(0xFFE4C6FF),
                        modifier = Modifier
                            .padding(2.dp)
                    )
                }

                IconButton(
                    onClick = {
                        navigate(
                            Routes.TIPS.getRoute()
                        )
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                ) {
                    // Icon for history button
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        tint = Color(0xFFE4C6FF),
                        contentDescription = "Lightbulb",
                        modifier = Modifier
                            .padding(2.dp)
                    )
                }

                IconButton(
                    onClick = {
                        navigate(
                            Routes.SETTINGS.getRoute()
                        )
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                ) {
                    // Icon for history button
                    Icon(
                        painter = painterResource(id = R.drawable.settings),
                        tint = Color(0xFFE4C6FF),
                        contentDescription = "Settings",
                        modifier = Modifier
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    SleepTheme {
        WearHome(
            navigate = {},
            wakeTime = Pair(
                LocalTime.of(10, 30),
                AlarmType.SYSTEM_ALARM
            ),
            nextAlarm = LocalTime.of(7, 30),
            timeManager = TimeManager(),
            null
        )
    }
}