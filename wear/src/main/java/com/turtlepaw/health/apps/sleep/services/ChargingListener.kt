package com.turtlepaw.health.apps.sleep.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.Keep
import com.turtlepaw.health.apps.sleep.common.BaseReceiver
import com.turtlepaw.shared.database.BedtimeSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Keep
class ChargingReceiver: BaseReceiver() {
    override val tag = "ChargingReceiver"
    override val sensorType = BedtimeSensor.CHARGING

    override fun onReceive(context: Context, intent: Intent) {
        val shouldRun = this.checkShouldRun(context, intent)
        if(shouldRun){
            Log.d(tag, "Received charging change... ($intent)")
            CoroutineScope(Dispatchers.Default).launch {
                runReceiver(context)
            }
        }
    }

    private suspend fun runReceiver(context: Context) {
        Log.d(tag, "Retrieving new charging state...")
        // This will only trigger when the watch is
        // plugged in, so we don't need to check
        Log.d(tag, "Saving new entry...")
        saveEntry(context, sensorType)
    }
}