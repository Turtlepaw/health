package com.turtlepaw.health.apps.sleep.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.core.content.edit
import com.turtlepaw.health.apps.sleep.utils.Settings
import com.turtlepaw.health.apps.sleep.utils.SettingsBasics

@Keep
class Receiver: BroadcastReceiver() {
    private var sharedPreferences: SharedPreferences? = null

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED){
            BedtimeModeReceiver().onReceive(context, intent)
        } else if(intent.action == Intent.ACTION_POWER_CONNECTED){
            ChargingReceiver().onReceive(context, intent)
        } else if(intent.action == "com.turtlepaw.sunlight.SUNLIGHT_CHANGED"){
            if(sharedPreferences == null) sharedPreferences = context.getSharedPreferences(
                SettingsBasics.SHARED_PREFERENCES.getKey(),
                SettingsBasics.SHARED_PREFERENCES.getMode()
            )

            val value = intent.getIntExtra("value", 0)
            sharedPreferences?.edit {
                putInt(Settings.SUNLIGHT.getKey(), value)
                commit()
            }
        }
    }
}