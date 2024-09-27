package com.turtlepaw.health.apps.sleep.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.data.UserActivityInfo
import androidx.health.services.client.data.UserActivityState
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.turtlepaw.health.apps.sleep.utils.Settings
import com.turtlepaw.health.apps.sleep.utils.SettingsBasics
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun createPassiveListenerConfig(): PassiveListenerConfig {
    return PassiveListenerConfig.builder()
        .setShouldUserActivityInfoBeRequested(true)
        .build()
}

fun getPassiveListenerCallback(sharedPreferences: SharedPreferences): PassiveListenerCallback {
    return object : PassiveListenerCallback {
        override fun onUserActivityInfoReceived(info: UserActivityInfo) {
            val stateChangeTime: Instant = info.stateChangeTime
            val userActivityState: UserActivityState = info.userActivityState
            Log.d("PassiveListener", "Received: $userActivityState")
            if (userActivityState == UserActivityState.USER_ACTIVITY_ASLEEP) {
                val zoneId = ZoneId.systemDefault()
                val localDateTime: LocalDateTime = stateChangeTime.atZone(zoneId).toLocalDateTime()
                sharedPreferences.edit {
                    putString(Settings.LAST_BEDTIME.getKey(), localDateTime.toString())
                }
            }
        }
    }
}

class HealthStartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HealthStartup", "Received intent: ${intent.action}")
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // TODO: Check permissions first
        WorkManager.getInstance(context).enqueueHealthWorker(context)
    }
}

fun WorkManager.enqueueHealthWorker(context: Context, force: Boolean = false) {
    val healthAvailable = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACTIVITY_RECOGNITION
    ) == PackageManager.PERMISSION_GRANTED

    if (!force && !healthAvailable) return

    enqueue(
        OneTimeWorkRequestBuilder<RegisterForPassiveDataWorker>()
            .build()
    )
}

class RegisterForPassiveDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        runBlocking {
            HealthServices.getClient(applicationContext)
                .passiveMonitoringClient
                .setPassiveListenerCallback(
                    createPassiveListenerConfig(),
                    getPassiveListenerCallback(
                        applicationContext.getSharedPreferences(
                            SettingsBasics.SHARED_PREFERENCES.getKey(),
                            SettingsBasics.SHARED_PREFERENCES.getMode()
                        )
                    )
                )
        }
        return Result.success()
    }
}
