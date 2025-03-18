package com.turtlepaw.health.apps.sleep.presentation.pages.settings

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BedtimeOff
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.sleep.presentation.Routes
import com.turtlepaw.health.apps.sleep.services.NotificationWorker
import com.turtlepaw.health.apps.sleep.utils.Settings
import com.turtlepaw.shared.components.Material3Page
import com.turtlepaw.shared.getDefaultSharedSettings
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


@OptIn(
    ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun WearSettings(
    navigate: (route: String) -> Unit,
    openAlarmSettings: () -> Unit,
    setAlerts: (value: Boolean) -> Unit,
    alerts: Boolean,
    context: Context,
    targetBedtime: LocalTime?
) {
    val sharedPrefs = context.getDefaultSharedSettings()
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    val permission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
        WorkManager.getInstance(context)
            .enqueueBedtimeNotification(targetBedtime)

        setAlerts(false)
    }

    var alarm by remember {
        mutableStateOf<LocalTime?>(null)
    }
    var bedtimeSync by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        alarm = sharedPrefs.getString(Settings.ALARM.getKey(), null).run {
            Log.d("AlarmSettings", "Alarm: $this")
            if (this == null) null
            else try {
                LocalTime.parse(this)
            } catch (e: Exception) {
                Log.e("AlarmSettings", "Error parsing alarm time: $this", e)
                null
            }
        }
        bedtimeSync = sharedPrefs.getBoolean(
            Settings.BEDTIME_SYNC.getKey(),
            Settings.BEDTIME_SYNC.getDefaultAsBoolean()
        )
    }

    Material3Page {
        item {
            Text(
                text = "Settings",
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        item {
            Button(
                onClick = {
                    openAlarmSettings()
                },
                modifier = Modifier
                    .fillMaxWidth(),
                label = {
                    Text(
                        text = "Alarm",
                    )
                },
                secondaryLabel = {
                    if (alarm == null) Text("Off")
                    else Text(
                        text = alarm!!.format(formatter),
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Alarm,
                        contentDescription = "schedule",
                    )
                },
            )
        }
//        item {
//            Button(
//                onClick = {
//                    navigate(Routes.SETTINGS_BEDTIME.getRoute())
//                },
//                modifier = Modifier
//                    .fillMaxWidth(),
//                icon = {
//                    Icon(
//                        imageVector = Icons.Rounded.Settings,
//                        contentDescription = "settings",
//                    )
//                }
//            ) {
//                Text(
//                    text = "Bedtime Settings",
//                )
//            }
//        }
        item {
            Button(
                onClick = {
                    navigate(Routes.CALIBRATION.getRoute())
                },
                modifier = Modifier
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_vital_signs),
                        contentDescription = "Vital signs",
                    )
                }
            ) {
                Text(
                    text = "Calibration",
                )
            }
        }
        item {
            SwitchButton(
                modifier = Modifier
                    .fillMaxWidth(),
                checked = bedtimeSync,
                onCheckedChange = { _isEnabled ->
                    bedtimeSync = _isEnabled
                    sharedPrefs.edit {
                        putBoolean(Settings.BEDTIME_SYNC.name, _isEnabled)
                    }
                },
                label = {
                    Text("Sync Bedtime Mode", overflow = TextOverflow.Ellipsis)
                },
                secondaryLabel = {
                    Text("with sleep tracking")
                },
                icon = {
                    Icon(
                        imageVector = if (bedtimeSync) Icons.Rounded.Bedtime else Icons.Rounded.BedtimeOff,
                        contentDescription = "Bedtime",
                        modifier = Modifier
                            .wrapContentSize(align = Alignment.Center),
                    )
                },
                enabled = true,
            )
        }
        item {
            SwitchButton(
                modifier = Modifier
                    .fillMaxWidth(),
                checked = alerts,
                onCheckedChange = { isEnabled ->
                    val workManager = WorkManager.getInstance(context)
                    if (isEnabled) {
                        if (permission.status.isGranted) {
                            workManager.enqueueBedtimeNotification(targetBedtime)
                            setAlerts(true)
                        } else {
                            permission.launchPermissionRequest()
                        }
                    } else {
                        workManager.cancelAllWorkByTag(NotificationWorker.WORKER_TAG)
                        setAlerts(false)
                    }
                },
                label = {
                    Text("Alerts", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                icon = {
                    Icon(
                        imageVector = if (alerts) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                        contentDescription = "alert",
                        modifier = Modifier
                            .wrapContentSize(align = Alignment.Center),
                    )
                },
                enabled = true,
            )
        }
        item {
            Text(
                text = "Proudly open-source",
                modifier = Modifier.padding(top = 10.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun WorkManager.enqueueBedtimeNotification(targetBedtime: LocalTime?) {
    var delay: Long
    val targetTime = targetBedtime ?: LocalTime.MIDNIGHT
    val now = LocalTime.now()
    if (targetTime.isAfter(now)) {
        // Calculate delay for today's target time
        val duration: Duration = Duration.between(now, targetTime)
        delay = duration.toMillis()
    } else {
        // Calculate delay for tomorrow's target time
        val duration: Duration =
            Duration.between(now, LocalTime.MIDNIGHT).plus(
                Duration.between(
                    LocalTime.MIDNIGHT, targetTime
                )
            )
        delay = duration.toMillis()
    }

    val request = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .addTag(NotificationWorker.WORKER_TAG)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

    enqueueUniqueWork(
        NotificationWorker.WORKER_TAG,
        ExistingWorkPolicy.REPLACE,
        request
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    WearSettings(
        navigate = {},
        openAlarmSettings = {},
        setAlerts = {},
        alerts = Settings.ALERTS.getDefaultAsBoolean(),
        context = LocalContext.current,
        targetBedtime = LocalTime.now()
    )
}