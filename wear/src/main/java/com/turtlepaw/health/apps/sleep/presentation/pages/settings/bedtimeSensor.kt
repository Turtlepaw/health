package com.turtlepaw.health.apps.sleep.presentation.pages.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.components.Page
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.utils.BedtimeSensor

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
        /**
         * Configure bedtime settings such as use bedtime, sensor (charging or bedtime mode), and force timeframe
         */
fun WearBedtimeSensorSetting(
    navigator: NavHostController,
    sensor: BedtimeSensor,
    setSensor: (value: BedtimeSensor) -> Unit
) {
        var state by remember { mutableStateOf(sensor) }
        Page {
//                item {
//                    Spacer(modifier = Modifier.padding(1.dp))
//                }
            item {
                Text(
                    text = "Sensor Source",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            item {
                ToggleChip(
                    modifier = Modifier
                        .fillMaxWidth(),
                    checked = state == BedtimeSensor.BEDTIME,
                    onCheckedChange = {
                        setSensor(BedtimeSensor.BEDTIME)
                        state = BedtimeSensor.BEDTIME
                    },
                    label = {
                        Text("Bedtime Mode", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    toggleControl = {
                        RadioButton(
                            selected = state == BedtimeSensor.BEDTIME,
                        )
                    },
                    enabled = true,
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedEndBackgroundColor = Color(0x80E4C6FF)
                    )
                )
            }
            item {
                ToggleChip(
                    modifier = Modifier
                        .fillMaxWidth(),
                    checked = state == BedtimeSensor.CHARGING,
                    onCheckedChange = {
                        setSensor(BedtimeSensor.CHARGING)
                        state = BedtimeSensor.CHARGING
                    },
                    label = {
                        Text("Charging", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    toggleControl = {
                        RadioButton(
                            selected = state == BedtimeSensor.CHARGING,
                        )
                    },
                    enabled = true,
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedEndBackgroundColor = Color(0x80E4C6FF)
                    )
                )
            }
    }
}