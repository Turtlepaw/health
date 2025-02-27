package com.turtlepaw.health

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import com.turtlepaw.health.services.Receiver

open class HealthApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // The following code is from home assistant:
        // https://github.com/home-assistant/android/
        val sensorReceiver = Receiver()

        // This will trigger for DND changes, including bedtime and theater mode
        registerReceiver(
            sensorReceiver,
            IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        )

        // This will listen for battery saver
        registerReceiver(
            sensorReceiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        )

        // This will cause the sensor to be updated every time the OS broadcasts that a cable was plugged/unplugged.
        // This should be nearly instantaneous allowing automations to fire immediately when a phone is plugged
        // in or unplugged. Updates will also be triggered when the system reports low battery and when it recovers.
        registerReceiver(
            sensorReceiver,
            IntentFilter(Intent.ACTION_POWER_CONNECTED)
        )
    }
}