package com.turtlepaw.health.apps.sleep.presentation.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.composables.TimePickerWith12HourClock
import com.turtlepaw.health.apps.sleep.utils.Settings
import java.time.LocalTime

@Composable
fun TimePicker(
    close: () -> Unit,
    defaultTime: LocalTime,
    setTime: (value: LocalTime) -> Unit
){
        TimePickerWith12HourClock(
            time = defaultTime,
            onTimeConfirm = { time ->
                setTime(time)
                close()
            }
        )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun WakeTimePickerPreview() {
    TimePicker(
        close = {},
        defaultTime = Settings.WAKE_TIME.getDefaultAsLocalTime(),
        setTime = {}
    )
}