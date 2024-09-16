package com.turtlepaw.health.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.IconCompat
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.turtlepaw.health.apps.health.complication.MainComplicationService
import com.turtlepaw.health.database.AppDatabase
import com.turtlepaw.health.database.SunlightDay
import com.turtlepaw.health.utils.Settings
import com.turtlepaw.health.utils.SettingsBasics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import kotlin.properties.Delegates


@Keep
class LightWorker : Service(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private lateinit var database: AppDatabase
    private var timeInLight: Int = 0
    private var lastUpdated: LocalTime = LocalTime.now()
    private var threshold: Int? = null
    private var minutes: Int = 0
    private var goal by Delegates.notNull<Int>()
    var context: Context = this
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var midnightRunnable: Runnable
    private lateinit var sharedPreferences: SharedPreferences
    private val thresholdReceiver = ThresholdReceiver()
    private val shutdownReceiver = ShutdownReceiver()
    private val wakeupReceiver = WakeupReceiver()
    private val goalReceiver = GoalReceiver()

    // Shared Preferences Listener
    inner class ThresholdReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received new light threshold")
            val defaultThreshold = Settings.SUN_THRESHOLD.getDefaultAsInt()
            // Update threshold value when received a broadcast
            val threshold = intent?.getIntExtra("threshold", defaultThreshold) ?: defaultThreshold
            updateThreshold(threshold)
        }
    }

    inner class GoalReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received new goal")
            val defaultGoal = Settings.GOAL.getDefaultAsInt()
            // Update threshold value when received a broadcast
            val goal = intent?.getIntExtra("goal", defaultGoal) ?: defaultGoal
            updateGoal(goal)
        }
    }

    inner class ShutdownReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received shutdown command")
            onShutdown()
        }
    }

    inner class WakeupReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received wakeup command")
            onWakeup()
        }
    }

    fun updateThreshold(newThreshold: Int) {
        threshold = newThreshold
        Log.d(TAG, "Threshold updated")
    }

    fun updateGoal(newGoal: Int) {
        goal = newGoal
        Log.d(TAG, "Goal updated")
    }

    fun onShutdown() {
        Log.d(TAG, "Shutting down...")
        sharedPreferences.edit {
            putBoolean(Settings.STATUS.getKey(), false)
        }
        unregisterReceiver(shutdownReceiver)
        handler.removeCallbacks(runnable)
        sensorManager!!.unregisterListener(this)
    }

    fun onWakeup() {
        Log.d(TAG, "Waking up...")
        sharedPreferences.edit {
            putBoolean(Settings.STATUS.getKey(), true)
        }
        val shutDownFilter = IntentFilter("${packageName}.SHUTDOWN_WORKER")
        registerReceiver(shutdownReceiver, shutDownFilter)
        sensorManager!!.registerListener(
            this, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        handler.postDelayed(runnable, 15000)
    }

    override fun onStart(intent: Intent?, startid: Int) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating light listener")
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
        database = AppDatabase.getDatabase(this)

        Toast.makeText(this, "Tracking", Toast.LENGTH_LONG).show()

        handler = Handler(Looper.myLooper()!!)
        runnable = Runnable {
            // handler to stop android
            // from hibernating this service
            Log.v(TAG, "Service still running, time in sunlight is $timeInLight")
            handler.postDelayed(runnable, 10000)
        }
        midnightRunnable = Runnable {
            minutes = 0
            val midnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            val currentTime = Calendar.getInstance()

            val delayInMillis = if (currentTime.timeInMillis > midnight.timeInMillis) {
                // If current time is already past midnight, schedule for the next day
                midnight.add(Calendar.DAY_OF_MONTH, 1)
                midnight.timeInMillis - currentTime.timeInMillis
            } else {
                midnight.timeInMillis - currentTime.timeInMillis
            }
            handler.postAtTime(midnightRunnable, delayInMillis)
        }

        handler.postDelayed(runnable, 15000)

        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val currentTime = Calendar.getInstance()

        val delayInMillis = if (currentTime.timeInMillis > midnight.timeInMillis) {
            // If current time is already past midnight, schedule for the next day
            midnight.add(Calendar.DAY_OF_MONTH, 1)
            midnight.timeInMillis - currentTime.timeInMillis
        } else {
            midnight.timeInMillis - currentTime.timeInMillis
        }

        handler.postAtTime(runnable, delayInMillis)

        val thresholdFilter = IntentFilter("${packageName}.THRESHOLD_UPDATED")
        registerReceiver(thresholdReceiver, thresholdFilter)
        val shutDownFilter = IntentFilter("${packageName}.SHUTDOWN_WORKER")
        registerReceiver(shutdownReceiver, shutDownFilter)
        val wakeupFilter = IntentFilter("${packageName}.WAKEUP_WORKER")
        registerReceiver(wakeupReceiver, wakeupFilter)
        val goalFilter = IntentFilter("${packageName}.GOAL_UPDATED")
        registerReceiver(goalReceiver, goalFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Waiting for light changes")
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        sharedPreferences.edit {
            putBoolean(Settings.STATUS.getKey(), true)
        }
        threshold = sharedPreferences.getInt(
            Settings.SUN_THRESHOLD.getKey(),
            Settings.SUN_THRESHOLD.getDefaultAsInt()
        )
        goal = sharedPreferences.getInt(
            Settings.GOAL.getKey(),
            Settings.GOAL.getDefaultAsInt()
        )
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

    fun sendNotification(context: Context, goal: Int) {
        // Create a notification channel for Android Oreo and higher
        val channelId = "goal_complete"
        val channelName = "Goal Complete"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Receive notifications when you reach your goal"
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Create the notification

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.turtlepaw.health.R.drawable.sunlight_gold)
            .setContentTitle("Goal Complete")
            .setContentText("You've completed your goal of ${goal}m!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            notify(123, builder.build())
        }
    }

    private fun saveMinutes(value: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = database.sunlightDao()
            dao.insertDay(
                SunlightDay(
                    timestamp = LocalDate.now(),
                    value = value
                )
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val luminance = event.values[0]

            if (threshold != null && luminance >= (threshold ?: 0)) {
                val currentTime = LocalTime.now()
                val timeSinceLastUpdate = Duration.between(lastUpdated, currentTime).toMillis()
                timeInLight += timeSinceLastUpdate.toInt()
                // Backwards compatible
                if (timeInLight >= 60000) {
                    CoroutineScope(Dispatchers.Default).launch {
                        minutes = if (minutes == 0) {
                            (database.sunlightDao().getDay(LocalDate.now())?.value
                                ?: 0) + (timeInLight / 1000 / 60)
                        } else {
                            minutes + (timeInLight / 1000 / 60).toInt()
                        }

                        saveMinutes(
                            minutes
                        )

                        minutes += (timeInLight / 1000 / 60).toInt()
                        timeInLight = 0

                        ComplicationDataSourceUpdateRequester
                            .create(
                                context = context,
                                complicationDataSourceComponent = ComponentName(
                                    context,
                                    MainComplicationService::class.java
                                )
                            )
                            .requestUpdateAll()

                        val intent = Intent("${context.packageName}.SUNLIGHT_CHANGED").apply {
                            putExtra("value", minutes)
                        }
                        context.sendBroadcast(intent)

                        if (minutes == goal) {
                            if (
                                sharedPreferences.getBoolean(
                                    Settings.GOAL_NOTIFICATIONS.getKey(),
                                    Settings.GOAL_NOTIFICATIONS.getDefaultAsBoolean()
                                )
                            )
                                sendNotification(context, goal)
                        }
                    }
                }
                lastUpdated = currentTime
            } else {
                lastUpdated = LocalTime.now() // Update lastUpdated even if not bright enough
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
//        sensorManager!!.unregisterListener(this)
//        stopSelf()
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
        // Clean up the sensor and service
    }

    companion object {
        private const val TAG = "LightWorker"
    }
}
