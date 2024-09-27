package com.turtlepaw.health.apps.sleep.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import com.turtlepaw.health.apps.sleep.common.BaseReceiver
import com.turtlepaw.health.database.BedtimeSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Keep
class BedtimeModeReceiver: BaseReceiver() {
    override val tag = "BedtimeModeReceiver"
    override val sensorType = BedtimeSensor.BEDTIME

    override fun onReceive(context: Context, intent: Intent) {
        val shouldRun = this.checkShouldRun(context, intent)
        if(shouldRun){
            Log.d(tag, "Received bedtime mode change... ($intent)")
            CoroutineScope(Dispatchers.Default).launch {
                delay(500)
                runReceiver(context)
            }
        }
    }

    private suspend fun runReceiver(context: Context) {
        Log.d(tag, "Retrieving new bedtime state...")
        // The following code is from home assistant:
        // https://github.com/home-assistant/android/blob/c6ddca8fdc34d2e7741ec82c04b7d8b8d01d3995/wear/src/main/java/io/homeassistant/companion/android/sensors/BedtimeModeSensorManager.kt#L52
        val state = try {
            Settings.Global.getInt(context.contentResolver, if (Build.MANUFACTURER == "samsung") "setting_bedtime_mode_running_state" else "bedtime_mode") == 1
        } catch (e: Exception) {
            Log.e(tag, "Failed to update bedtime mode sensor", e)
            false
        }

        if(state){
            Log.d(tag, "Bedtime mode is on, adding entry")
            saveEntry(context, sensorType)
        } else Log.d(tag, "Bedtime mode is off, not adding entry")
    }
}