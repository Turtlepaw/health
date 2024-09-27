package com.turtlepaw.health.apps.sleep.presentation.pages.settings

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.sleep.presentation.Routes
import com.turtlepaw.health.apps.sleep.services.NotificationWorker
import com.turtlepaw.health.apps.sleep.utils.Settings
import com.turtlepaw.health.components.Page
import com.turtlepaw.sleeptools.utils.AlarmType
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
    openWakeTimePicker: () -> Unit,
    wakeTime: Pair<LocalTime, AlarmType>,
    userWakeTime: LocalTime,
    setAlarm: (value: Boolean) -> Unit,
    useAlarm: Boolean,
    setAlerts: (value: Boolean) -> Unit,
    alerts: Boolean,
    context: Context,
    targetBedtime: LocalTime?
) {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val permission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
            WorkManager.getInstance(context)
                .enqueueBedtimeNotification(targetBedtime)

            setAlerts(false)
        }

        Page {
            item {
                Text(
                    text = "Settings",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            item {
                Chip(
                    onClick = {
                        openWakeTimePicker()
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(
                            text = "Wake Up",
                            color = MaterialTheme.colors.onPrimary
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = userWakeTime.format(formatter),
                            color = MaterialTheme.colors.onPrimary
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = "schedule",
                            tint = MaterialTheme.colors.onPrimary,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                )
            }
            item {
                Button(
                    onClick = {
                        navigate(Routes.SETTINGS_BEDTIME.getRoute())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "settings",
                            tint = MaterialTheme.colors.onPrimary,
                            modifier = Modifier
                                .padding(2.dp)
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(
                            text = "Bedtime Settings",
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            }
            item {
                ToggleChip(
                    modifier = Modifier
                        .fillMaxWidth(),
                    checked = useAlarm,
                    onCheckedChange = { isEnabled ->
                        setAlarm(isEnabled)
                    },
                    label = {
                        Text("Alarm", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    appIcon = {
                        Icon(
                            imageVector = if (useAlarm) Icons.Rounded.Alarm else Icons.Rounded.AlarmOff,
                            contentDescription = "alarm",
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    },
                    toggleControl = {
                        Switch(
                            checked = useAlarm,
                            enabled = true,
                            modifier = Modifier.semantics {
                                this.contentDescription =
                                    if (useAlarm) "On" else "Off"
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
                ToggleChip(
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
                    appIcon = {
                        Icon(
                            imageVector = if (alerts) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                            contentDescription = "alert",
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    },
                    toggleControl = {
                        Switch(
                            checked = alerts,
                            enabled = true,
                            modifier = Modifier.semantics {
                                this.contentDescription =
                                    if (alerts) "On" else "Off"
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
                Text(
                    text = "This project is open-source: turtlepaw/sleep (github.com)",
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
        openWakeTimePicker = {},
        wakeTime = Pair(
            Settings.WAKE_TIME.getDefaultAsLocalTime(),
            AlarmType.SYSTEM_ALARM
        ),
        userWakeTime = Settings.WAKE_TIME.getDefaultAsLocalTime(),
        setAlarm = {},
        useAlarm = Settings.ALARM.getDefaultAsBoolean(),
        setAlerts = {},
        alerts = Settings.ALERTS.getDefaultAsBoolean(),
        context = LocalContext.current,
        targetBedtime = LocalTime.now()
    )
}