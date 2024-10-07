package com.turtlepaw.heartconnect.presentation.pages

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.LocationAvailability
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.curvedText
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.exercise.manager.ExerciseViewModel
import com.turtlepaw.health.apps.exercise.manager.FakeExerciseViewModel
import com.turtlepaw.health.apps.exercise.presentation.Routes
import com.turtlepaw.health.components.Page
import com.turtlepaw.heart_connection.Exercise
import com.turtlepaw.heart_connection.Exercises
import com.turtlepaw.heartconnect.presentation.components.StartButton
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun ExerciseConfiguration(
    exercise: Exercise,
    navigate: (route: String) -> Unit,
    id: Int,
    context: Context,
    heartRate: Int?,
    exerciseViewModel: ExerciseViewModel,
    onStart: () -> Unit
) {
    ExerciseTheme {
        val coroutineScope = rememberCoroutineScope()
        val progress = remember { Animatable(0f) }
        val availability = exerciseViewModel.availablities.observeAsState()
        val location =
            availability.value?.values?.filterIsInstance<LocationAvailability>()?.firstOrNull()

        LaunchedEffect(Unit) {
            exerciseViewModel.warmExerciseSession(exercise)
        }

        Page(
            startTimeTextLinear = {
                Text(
                    text = updatePrepareLocationStatus(
                        locationAvailability = location ?: LocationAvailability.UNAVAILABLE
                    )
                )
            },
            startTimeTextCurved = {
                curvedText(
                    text = updatePrepareLocationStatus(
                        locationAvailability = location ?: LocationAvailability.UNAVAILABLE
                    ),
                    style = CurvedTextStyle()
                )
            },
        ) {
            item {
                Text(
                    text = exercise.name,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
            }
            item {
                StartButton(progress) {
                    coroutineScope.launch {
                        val vibrator = context.getSystemService(Vibrator::class.java)
                        progress.snapTo(1f)

                        // Initial "Tick"
                        if (vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(
                                VibrationEffect.startComposition().addPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_TICK, 1f
                                ).compose()
                            )
                        }

                        progress.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 3500)
                        )

                        if (vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(
                                VibrationEffect.startComposition().addPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 1f
                                ).addPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1f, 400
                                ).compose()
                            )
                        }

                        exerciseViewModel.startExercise()
                        delay(100)
                        onStart()
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.padding(10.dp))
            }
            item {
                Chip(
                    onClick = { navigate(Routes.PAIR_DEVICE.getRoute()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.bluetooth_searching),
                            contentDescription = "Bluetooth Searching"
                        )
                    },
                    label = {
                        Text(
                            text = "Connect Sensor"
                        )
                    },
                    secondaryLabel = heartRate?.let { bpm ->
                        { Text(text = "${bpm}bpm") }
                    }
                )
            }
            item {
                Chip(
                    onClick = { navigate(Routes.METRIC_EDITOR.getRoute(id.toString())) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.build),
                            contentDescription = "Build"
                        )
                    },
                    label = {
                        Text(
                            text = "Metrics"
                        )
                    }
                )
            }
        }
    }
}


/*
    Modified from https://github.com/android/health-samples/tree/main/health-services/ExerciseSampleCompose
 */
private fun updatePrepareLocationStatus(locationAvailability: LocationAvailability): String {
    return when (locationAvailability) {
        LocationAvailability.ACQUIRED_TETHERED, LocationAvailability.ACQUIRED_UNTETHERED -> "GPS Connected"
        LocationAvailability.NO_GNSS -> "GPS Disabled" // TODO Consider redirecting user to change device settings in this case
        LocationAvailability.ACQUIRING -> "GPS Connecting"
        LocationAvailability.UNKNOWN -> "GPS Initializing"
        else -> "GPS Unavailable"
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ExerciseConfigurationPreview() {
    ExerciseConfiguration(
        Exercises.first(),
        {},
        1,
        LocalContext.current,
        86,
        FakeExerciseViewModel(Application())
    ) {}
}

