package com.turtlepaw.health.apps.exercise.manager

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.endExercise
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.prepareExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.exercise.presentation.MainActivity
import com.turtlepaw.health.utils.HealthNotifications
import com.turtlepaw.health.utils.Settings
import com.turtlepaw.health.utils.SettingsBasics
import com.turtlepaw.heart_connection.Exercise
import com.turtlepaw.heart_connection.Exercises
import com.turtlepaw.heart_connection.HeartConnection
import com.turtlepaw.heart_connection.Workout
import com.turtlepaw.heart_connection.createGattCallback
import kotlinx.coroutines.*

enum class HeartRateSource {
    HeartRateMonitor,
    Device
}

class ExerciseService : Service() {
    companion object {
        const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL =
            "FOREGROUND"
        private const val NOTIFICATION_CHANNEL_DISPLAY = "Ongoing Exercise"
        private const val NOTIFICATION_TITLE = "Heart Connect"
        private const val NOTIFICATION_TEXT = "Ongoing Exercise"
        private const val ONGOING_STATUS_TEMPLATE = "Ongoing Exercise #duration#"
    }

    private val heartRateLiveData = MutableLiveData<Int>()
    private val caloriesLiveData = MutableLiveData<Double>()
    private val distanceLiveData = MutableLiveData<Double>()
    private val durationLiveData = MutableLiveData<Long>()
    private val heartRateSourceData = MutableLiveData<HeartRateSource>(HeartRateSource.Device)
    private val rawData = MutableLiveData<ExerciseUpdate>()
    private val availabilities = MutableLiveData<Map<DataType<*, *>, Availability>>(emptyMap())

    private var exerciseClient: ExerciseClient? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val binder = LocalBinder()
    private var exerciseType: Exercise? = null

    inner class LocalBinder : Binder() {
        fun getService(): ExerciseService = this@ExerciseService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        val healthServicesClient = HealthServices.getClient(this)
        exerciseClient = healthServicesClient.exerciseClient

        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        coroutineScope.launch {
//            startExerciseSession()
//        }
        return START_STICKY
    }

    suspend fun warmExerciseSession(type: Exercise) {
        val metrics = setOf(DataType.HEART_RATE_BPM)
        if (type.useGps) metrics.plus(DataType.LOCATION)

        exerciseType = type
        exerciseClient?.prepareExercise(
            WarmUpConfig(
                type.mapped,
                metrics
            )
        )

        startHeartRateTracking()
    }

    suspend fun startExerciseSession() {
        exerciseClient?.startExercise(
            ExerciseConfig.builder((exerciseType ?: Exercises.first()).mapped)
                .setIsGpsEnabled((exerciseType ?: Exercises.first()).useGps == true).build()
        )
        startHeartRateTracking()
    }

    suspend fun stopExerciseSession() {
        exerciseClient?.endExercise()
    }

    suspend fun pauseExerciseSession() {
        exerciseClient?.pauseExercise()
    }

    suspend fun resumeExerciseSession() {
        exerciseClient?.resumeExercise()
    }

    private fun startHeartRateTracking() {
        exerciseClient?.setUpdateCallback(exerciseUpdateListener)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            var bluetoothAdapter = getSystemService(BluetoothManager::class.java)?.adapter

            val connection = HeartConnection(
                createGattCallback {
                    heartRateLiveData.postValue(it)
                },
                applicationContext,
                application
            )

            val sharedPreferences = getSharedPreferences(
                SettingsBasics.SHARED_PREFERENCES.getKey(),
                SettingsBasics.SHARED_PREFERENCES.getMode()
            )

            val macId = sharedPreferences.getString(
                Settings.DEFAULT_DEVICE.getKey(),
                Settings.DEFAULT_DEVICE.getDefaultOrNull()
            ) ?: return
            if (macId == "null") return

            Log.d("MacId", macId)
            val device = bluetoothAdapter?.getRemoteDevice(macId)
                ?: return

            connection.connectToDevice(device)
        }
//        } else {
//            coroutineScope.launch {
//                while (true) {
//                    val simulatedHeartRate = (60..150).random() // Simulated data
//                    heartRateLiveData.postValue(simulatedHeartRate)
//                    delay(5000)
//                }
//            }
//        }
    }

    fun attemptToReconnect() {
        startHeartRateTracking()
    }

    private val exerciseUpdateListener = object : ExerciseUpdateCallback {
        override fun onAvailabilityChanged(
            dataType: DataType<*, *>,
            data: Availability
        ) {
            availabilities.postValue(
                (availabilities.value ?: emptyMap()).toMutableMap().plus(Pair(dataType, data))
            )
        }

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val heartRate =
                update.latestMetrics.getData(DataType.HEART_RATE_BPM)?.lastOrNull()?.value?.toInt()
                    ?: 0
            heartRateLiveData.postValue(heartRate)

            val calories =
                update.latestMetrics.getData(DataType.CALORIES)?.lastOrNull()?.value ?: 0.0
            caloriesLiveData.postValue(calories)

            val distance =
                update.latestMetrics.getData(DataType.DISTANCE)?.lastOrNull()?.value ?: 0.0
            distanceLiveData.postValue(distance)

            val duration = update.activeDurationCheckpoint?.activeDuration?.seconds ?: 0L
            durationLiveData.postValue(duration)

            rawData.postValue(update)
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {

        }

        override fun onRegistered() {

        }

        override fun onRegistrationFailed(throwable: Throwable) {

        }
    }

    // Expose LiveData for external observers
    fun getHeartRateLiveData(): LiveData<Int> = heartRateLiveData
    fun getCaloriesLiveData(): LiveData<Double> = caloriesLiveData
    fun getDistanceLiveData(): LiveData<Double> = distanceLiveData
    fun getDurationLiveData(): LiveData<Long> = durationLiveData
    fun getRawDataLiveData(): LiveData<ExerciseUpdate> = rawData
    fun getHeartRateSourceData(): LiveData<HeartRateSource> = heartRateSourceData
    fun getAvailability(): LiveData<Map<DataType<*, *>, Availability>> = availabilities

    private fun startForegroundService() {
        // Make an intent that will take the user straight to the exercise UI.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        HealthNotifications().createChannel(this)

        // Build the notification.
        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, HealthNotifications.NOTIFICATION_CHANNEL)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_TEXT)
                .setSmallIcon(
                    (exerciseType ?: Workout).icon
                )
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        //val startMillis = SystemClock.elapsedRealtime() - duration.toMillis()
        val ongoingActivityStatus = Status.Builder()
            .addTemplate(ONGOING_STATUS_TEMPLATE)
            .addPart("duration", Status.StopwatchPart(durationLiveData.value?.toLong() ?: 0L))
            .build()
        val ongoingActivity =
            OngoingActivity.Builder(
                applicationContext,
                NOTIFICATION_ID, notificationBuilder
            )
                .setAnimatedIcon(R.drawable.exercise)
                .setStaticIcon(R.drawable.exercise)
                .setTouchIntent(pendingIntent)
                .setStatus(ongoingActivityStatus)
                .build()

        ongoingActivity.apply(applicationContext)

        startForeground(1, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}