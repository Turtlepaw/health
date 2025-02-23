package com.turtlepaw.health.apps.sleep.presentation.pages.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.sleep.presentation.Routes
import com.turtlepaw.shared.components.Page
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
/**
 * Configure bedtime settings such as use bedtime, sensor (charging or bedtime mode), and force timeframe
 */
fun WearBedtimeSettings(
    navigator: NavHostController,
    startTime: LocalTime,
    setTimeStart: (value: LocalTime) -> Unit,
    endTime: LocalTime,
    setTimeEnd: (value: LocalTime) -> Unit,
    timeframeEnabled: Boolean,
    setTimeframe: (value: Boolean) -> Unit
){
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        var state by remember { mutableStateOf(timeframeEnabled) }
        Page {
            item {
                Text(
                    text = "Bedtime Settings",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            item {
                Button(
                    onClick = {
                        navigator.navigate(Routes.SETTINGS_BEDTIME_SENSOR.getRoute())
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.filledVariantButtonColors(),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Sensors,
                            contentDescription = "Sensor",
                            modifier = Modifier
                                .size(24.dp)
                        )
                    },
                ) {
                    Text(
                        text = "Sensor Source",
                    )
                }
            }
            item {
                SwitchButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    checked = state,
                    onCheckedChange = { isEnabled ->
                        setTimeframe(isEnabled)
                        state = isEnabled
                    },
                    label = {
                        Text("Timeframe", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            //painter = painterResource(id = R.drawable.rounded_calendar_clock_24),
                            contentDescription = "edit calendar",
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    },
                    enabled = true,
                )
            }
            item {
                Button(
                    enabled = state,
                    onClick = {
                        navigator.navigate(Routes.SETTINGS_TIMEFRAME_START.getRoute())
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(
                            text = "Timeframe Start",
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = formatter.format(startTime),
                        )
                    },
                    colors = ButtonDefaults.filledTonalButtonColors()
                )
            }
            item {
                Button(
                    enabled = state,
                    onClick = {
                        navigator.navigate(Routes.SETTINGS_TIMEFRAME_END.getRoute())
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(
                            text = "Timeframe End",
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = formatter.format(endTime),
                        )
                    },
                    colors = ButtonDefaults.filledTonalButtonColors()
                )
            }
    }
}