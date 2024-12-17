package com.turtlepaw.health.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.turtlepaw.health.apps.sunlight.presentation.dataStore
import com.turtlepaw.health.utils.SunlightViewModel
import com.turtlepaw.health.utils.SunlightViewModelFactory
import com.turtlepaw.shared.Settings
import com.turtlepaw.shared.SettingsBasics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate


@Keep
class LightLoggerService : Service(), SensorEventListener, ViewModelStoreOwner {
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private lateinit var sunlightViewModel: SunlightViewModel
    override val viewModelStore = ViewModelStore()

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "sunlight",
            "Sunlight",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )

        val notification = NotificationCompat.Builder(this, "sunlight")
            .setSmallIcon(
                IconCompat.createFromIcon(
                    this,
                    android.graphics.drawable.Icon.createWithResource(
                        this,
                        com.turtlepaw.health.R.drawable.sunlight,
                    )
                )!!
            )
            .setLargeIcon(
                android.graphics.drawable.Icon.createWithResource(
                    this,
                    com.turtlepaw.health.R.drawable.sunlight,
                )
            )
            .setContentTitle("Listening for light")
            .setContentText("Listening for changes in light from your device").build()

        startForeground(1, notification)
        sunlightViewModel = ViewModelProvider(this, SunlightViewModelFactory(dataStore)).get(SunlightViewModel::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Waiting for light changes")
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
//        val factory = SunlightViewModelFactory(this.dataStore)
//        sunlightViewModel = ViewModelProvider(
//            applicationContext as ViewModelStoreOwner,
//            factory
//        )[SunlightViewModel::class.java]
        sensorManager!!.registerListener(
            this, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val sharedPreferences = getSharedPreferences(
                SettingsBasics.SHARED_PREFERENCES.getKey(),
                SettingsBasics.SHARED_PREFERENCES.getMode()
            )
            val threshold = sharedPreferences.getInt(
                Settings.SUN_THRESHOLD.getKey(),
                Settings.SUN_THRESHOLD.getDefaultAsInt()
            )
            val luminance = event.values[0]
            Log.d(TAG, "Received light: $luminance")

            if (luminance >= threshold) {
                CoroutineScope(Dispatchers.Default).launch {
                    Log.d(TAG, "Rewarding 1 minute")
                    sunlightViewModel.addMinute(LocalDate.now())
                }
            } else Log.d(TAG, "Not bright enough (target: $threshold)")

            // Clean up the sensor and service
            sensorManager!!.unregisterListener(this)
            stopSelf()
        }
    }

    companion object {
        private const val TAG = "LightLoggerService"
    }
}
