package com.turtlepaw.health.apps.sleep.presentation.pages.settings

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.turtlepaw.health.apps.sleep.utils.Settings
import com.turtlepaw.shared.components.Material3Page
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmSettings(
    sharedPrefs: SharedPreferences,
    openAlarmPicker: () -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    var alarm by remember {
        mutableStateOf<LocalTime?>(null)
    }

    LaunchedEffect(Unit) {
        alarm = sharedPrefs.getString(Settings.ALARM.getKey(), null).run {
            if (this == null) null
            else try {
                LocalTime.parse(this)
            } catch (e: Exception) {
                null
            }
        }
    }

    Material3Page {
        item {
            Text(
                text = "Alarm Settings"
            )
        }
        item {
            Spacer(
                modifier = Modifier.height(2.dp)
            )
        }
        item {
            SwitchButton(
                onCheckedChange = {
                    alarm = if (it == true) {
                        LocalTime.now()
                    } else {
                        null
                    }

                    sharedPrefs.edit { putString(Settings.ALARM.getKey(), alarm.toString()) }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                label = {
                    Text(
                        text = "Alarm",
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Alarm,
                        contentDescription = "Alarm",
                    )
                },
                checked = alarm != null,
            )
        }
        item {
            Button(
                onClick = {
                    openAlarmPicker()
                },
                modifier = Modifier
                    .fillMaxWidth(),
                label = {
                    Text(
                        text = "Time",
                    )
                },
                secondaryLabel = {
                    if (alarm == null) null
                    else Text(
                        text = alarm!!.format(formatter),
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = "schedule",
                    )
                },
                enabled = alarm != null
            )
        }
    }
}