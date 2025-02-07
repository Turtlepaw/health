package com.turtlepaw.health.apps.sleep.presentation.pages.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
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
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFE4C6FF)
                    )
                ) {
                    Text(
                        text = "Sensor Source",
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }
            item {
                ToggleChip(
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
                    appIcon = {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            //painter = painterResource(id = R.drawable.rounded_calendar_clock_24),
                            contentDescription = "edit calendar",
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    },
                    toggleControl = {
                        Switch(
                            checked = state,
                            enabled = true,
                            modifier = Modifier.semantics {
                                this.contentDescription =
                                    if (state) "On" else "Off"
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFE4C6FF)
                            )
                        )
                    },
                    enabled = true,
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedEndBackgroundColor = Color(0x80E4C6FF)
                    )
                )
            }
            item {
                Chip(
                    enabled = state,
                    onClick = {
                        navigator.navigate(Routes.SETTINGS_TIMEFRAME_START.getRoute())
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(
                            text = "Timeframe Start",
                            color = MaterialTheme.colors.onPrimary
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = formatter.format(startTime),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                )
            }
            item {
                Chip(
                    enabled = state,
                    onClick = {
                        navigator.navigate(Routes.SETTINGS_TIMEFRAME_END.getRoute())
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(
                            text = "Timeframe End",
                            color = MaterialTheme.colors.onPrimary
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = formatter.format(endTime),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                )
            }
    }
}