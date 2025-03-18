package com.turtlepaw.health.apps.sleep.presentation.pages.settings

import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Reviews
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.sleep.SleepCalibration
import com.turtlepaw.shared.components.Material3Page
import com.turtlepaw.shared.database.AppDatabase
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun Calibration() {
    val context = LocalContext.current
    val sleepCalibration = remember { SleepCalibration(context) }
    var calibrationState by remember { mutableStateOf(sleepCalibration.calibrationState) }
    var sessionCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val sessions = AppDatabase.getDatabase(context).sleepDao().getSessionsSince(
            LocalDateTime.now().minusDays(7)
        )
        sessionCount = sessions.size
        calibrationState = sleepCalibration.calibrationState
    }

    Material3Page {
        item {
            AnimatedCalibrationIcon(calibrationState, sessionCount)
        }
        item {
            Spacer(modifier = Modifier.size(12.dp))
        }
        item {
            CalibrationStatusText(
                state = calibrationState,
                sessionCount = sessionCount
            )
        }
        item {
            CalibrationDescriptionText(calibrationState)
        }

        val list: Map<Any, String> = when (calibrationState) {
            SleepCalibration.CalibrationState.LEARNING ->
                mapOf(
                    R.drawable.ic_vital_signs to "Nighttime heart rate trends",
                    R.drawable.ic_waves to "Movement variance",
                    R.drawable.ic_hotel to "Sleep duration patterns"
                )

            SleepCalibration.CalibrationState.STABLE ->
                mapOf(
                    Icons.Rounded.Mood to "Daily feedback adjustments",
                    Icons.Rounded.Reviews to "Weekly pattern reviews",
                    Icons.Rounded.Spa to "Automatic lifestyle changes detection"
                )

            SleepCalibration.CalibrationState.NEEDS_REFRESH ->
                emptyMap()
        }

        if (list.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.size(5.dp))
            }
        }

        items(list.toList()) {
            Card(
                enabled = false,
                onClick = { }
            ) {
                Row {
                    Icon(
                        painterResource(it.first as Int),
                        contentDescription = "",
                        modifier = Modifier.size(25.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        it.second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (calibrationState == SleepCalibration.CalibrationState.NEEDS_REFRESH) {
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            sleepCalibration.calculateBaselines()
                            calibrationState = sleepCalibration.calibrationState
                            Log.d("Calibration", "Calibration state: $calibrationState")
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    }
                    Text("Improve Accuracy")
                }
            }
        }
    }
}

@Composable
private fun AnimatedCalibrationIcon(state: SleepCalibration.CalibrationState, sessionCount: Int) {
    val transition = updateTransition(state, label = "calibrationIcon")
    val color by transition.animateColor(label = "iconColor") { state ->
        when (state) {
            SleepCalibration.CalibrationState.LEARNING -> MaterialTheme.colorScheme.primaryContainer
            SleepCalibration.CalibrationState.STABLE -> MaterialTheme.colorScheme.primary
            SleepCalibration.CalibrationState.NEEDS_REFRESH -> MaterialTheme.colorScheme.error
        }
    }
    val onColor by transition.animateColor(label = "onIconColor") { state ->
        when (state) {
            SleepCalibration.CalibrationState.LEARNING -> MaterialTheme.colorScheme.primary
            SleepCalibration.CalibrationState.STABLE -> MaterialTheme.colorScheme.onPrimary
            SleepCalibration.CalibrationState.NEEDS_REFRESH -> MaterialTheme.colorScheme.onError
        }
    }

    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
//        CircularProgressIndicator(
//            progress = { sessionCount / 7f },
//            colors = ProgressIndicatorDefaults.colors(
//                trackColor = color,
//                indicatorColor = color
//            ),
//            modifier = Modifier.fillMaxSize()
//        )
        Icon(
            painterResource(R.drawable.circle),
            contentDescription = "Circle",
            tint = color,
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            painterResource(R.drawable.ic_vital_signs),
            contentDescription = "Calibration status",
            tint = onColor,
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        )
    }
}

@Composable
private fun CalibrationStatusText(
    state: SleepCalibration.CalibrationState,
    sessionCount: Int
) {
    Text(
        text = when (state) {
            SleepCalibration.CalibrationState.LEARNING -> "Calibration Learning (${sessionCount}/7 days)"
            SleepCalibration.CalibrationState.STABLE -> "Calibration Stable"
            SleepCalibration.CalibrationState.NEEDS_REFRESH -> "Accuracy Improvement Available"
        },
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CalibrationDescriptionText(state: SleepCalibration.CalibrationState) {
    Text(
        text = when (state) {
            SleepCalibration.CalibrationState.LEARNING ->
                "We're analyzing your sleep patterns using"

            SleepCalibration.CalibrationState.STABLE ->
                "Your baseline is established but keeps adapting"

            SleepCalibration.CalibrationState.NEEDS_REFRESH ->
                "New sleep patterns have been detected."
        },
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true)
@Composable
fun CalibrationPreview() {
    MaterialTheme {
        Calibration()
    }
}