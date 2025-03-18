package com.turtlepaw.health.utils

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri

class PowerManagementHelper(private val context: Context) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun isIgnoringBatteryOptimizations(): Boolean {
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun createIgnoreBatteryOptimizationIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = "package:${context.packageName}".toUri()
        }
    }

    fun createWakeLock(tag: String): PowerManager.WakeLock {
        return powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SleepTracker::$tag"
        ).apply {
            setReferenceCounted(false)
        }
    }
}