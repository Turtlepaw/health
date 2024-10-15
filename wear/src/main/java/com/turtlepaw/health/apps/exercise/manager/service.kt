package com.turtlepaw.health.apps.exercise.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.endExercise
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.turtlepaw.health.apps.exercise.presentation.MainActivity
import com.turtlepaw.health.database.AppDatabase
import com.turtlepaw.health.database.ServiceType
import com.turtlepaw.health.database.exercise.Preference
import com.turtlepaw.health.utils.HealthNotifications
import com.turtlepaw.health.utils.Settings
import com.turtlepaw.health.utils.SettingsBasics
import com.turtlepaw.heart_connection.Exercise
import com.turtlepaw.heart_connection.Exercises
import com.turtlepaw.heart_connection.HeartConnection
import com.turtlepaw.heart_connection.SunlightMetric
import com.turtlepaw.heart_connection.createGattCallback
import com.turtlepaw.heart_connection.getId
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.time.toJavaDuration

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

    private val heartRateHistoryLiveData = MutableLiveData<List<Pair<LocalTime, Int>>>(emptyList())
    private val deviceHrHistoryLiveData = MutableLiveData<List<Pair<LocalTime, Int>>>(emptyList())
    private val externalHistoryLiveData = MutableLiveData<List<Pair<LocalTime, Int>>>(emptyList())

    private val heartRateLiveData = MutableLiveData<Int>()
    private val caloriesLiveData = MutableLiveData<Double>()
    private val distanceLiveData = MutableLiveData<Double>()
    private val stepsLiveData = MutableLiveData<Long>()
    private val durationLiveData = MutableLiveData<Duration>()
    private val heartRateSourceData = MutableLiveData<HeartRateSource>(HeartRateSource.Device)
    private val rawData = MutableLiveData<ExerciseUpdate?>(null)
    private val availabilities = MutableLiveData<Map<DataType<*, *>, Availability>>(emptyMap())
    private val sunlightLiveData = MutableLiveData<Int>()

    private var exerciseClient: ExerciseClient? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val binder = LocalBinder()
    private var exerciseType: Exercise? = null
    private var isInProgress = false
    private var connection: HeartConnection? = null
    private var startTime: LocalDateTime? = null
    private var zeroStartTime: Long? = null
    private var preferences: Preference? = null
    private var database = AppDatabase.getDatabase(this)

    inner class LocalBinder : Binder() {
        fun getService(): ExerciseService = this@ExerciseService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        val healthServicesClient = HealthServices.getClient(this)
        exerciseClient = healthServicesClient.exerciseClient

        //startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        coroutineScope.launch {
//            startExerciseSession()
//        }
        val exercise = Exercises.elementAtOrNull(intent?.getIntExtra("exerciseId", 0) ?: 0)
            ?: Exercises.first()
        startForegroundService(exercise)
        return START_STICKY
    }

    fun getCurrentExercise(): Exercise? {
        return exerciseType
    }

    suspend fun getPreferences(exercise: Exercise): Preference {
        return database.preferenceDao().getPreference(exercise.getId())
    }

    suspend fun collectSunlight() {
        val isEnabled =
            database.serviceDao().getService(ServiceType.SUNLIGHT.serviceName)?.isEnabled == true
        if (!isEnabled) return sunlightLiveData.postValue(-1)

        database.sunlightDao().getLiveDay(LocalDate.now())
            .collect { data ->
                sunlightLiveData.postValue(data?.value ?: 0)
            }
    }

    suspend fun startExerciseSession(exercise: Exercise) {
        // Clear any existing data
        heartRateHistoryLiveData.postValue(emptyList())
        deviceHrHistoryLiveData.postValue(emptyList())
        externalHistoryLiveData.postValue(emptyList())
        caloriesLiveData.postValue(0.0)
        distanceLiveData.postValue(0.0)
        stepsLiveData.postValue(0)
        durationLiveData.postValue(Duration.ZERO)
        heartRateSourceData.postValue(HeartRateSource.Device)
        availabilities.postValue(emptyMap())
        rawData.postValue(null)
        startTime = LocalDateTime.now()
        zeroStartTime = SystemClock.elapsedRealtime()
        preferences = getPreferences(exercise)

        if (preferences?.metrics?.contains(SunlightMetric) == true) {
            collectSunlight()
        }

        Log.d(
            "ExerciseService",
            "Starting exercise session with data types: ${
                exercise.dataTypes.map { it.name }.joinToString(",")
            }"
        )
        exerciseType = exercise
        exerciseClient?.startExercise(
            ExerciseConfig.builder(exercise.mapped)
                .setDataTypes(
                    exercise.dataTypes
                )
                .setIsGpsEnabled(exercise.useGps == true).build()
        )
        startHeartRateTracking()
        isInProgress = true
    }

    @SuppressLint("MissingPermission")
    suspend fun stopExerciseSession() {
        exerciseClient?.endExercise()
        try {
            AppDatabase.getDatabase(this).exerciseDao().insertExercise(
                com.turtlepaw.health.database.exercise.Exercise(
                    timestamp = startTime ?: LocalDateTime.now(),
                    exercise = (exerciseType ?: Exercises.first()).getId(),
                    averageHeartRate = heartRateHistoryLiveData.value?.map { it.second }?.average()
                        ?: 0.0,
                    totalDistance = distanceLiveData.value,
                    totalCalories = caloriesLiveData.value,
                    elapsedTime = durationLiveData.value ?: Duration.ZERO,
                    maxHeartRate = heartRateHistoryLiveData.value?.maxByOrNull { it.second }?.second
                        ?: 0,
                    heartRateHistory = heartRateHistoryLiveData.value ?: emptyList(),
                    sunlight = sunlightLiveData.value ?: 0,
                    steps = stepsLiveData.value,
                    heartRateSimilarity = if (canCompareHeartRates(
                            deviceHrHistoryLiveData.value ?: emptyList(),
                            externalHistoryLiveData.value ?: emptyList()
                        )
                    ) {
                        compareHeartRates(
                            deviceHrHistoryLiveData.value ?: emptyList(),
                            externalHistoryLiveData.value ?: emptyList()
                        )
                    } else {
                        null
                    },
                )
            )
        } catch (e: Exception) {
            Log.e("ExerciseService", "Error inserting exercise data", e)
        }

        isInProgress = false
        try {
            connection?.disconnect()
        } catch (e: Exception) {
            Log.e("ExerciseService", "Error disconnecting from device", e)
        }
        Log.d("ExerciseService", "Stopping exercise session")
        delay(1000)
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        coroutineScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
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

            connection = HeartConnection(
                createGattCallback {
                    // Prioritize peripherals
                    heartRateLiveData.postValue(it)
                    heartRateSourceData.postValue(HeartRateSource.HeartRateMonitor)
                    heartRateHistoryLiveData.postValue(
                        (heartRateHistoryLiveData.value ?: emptyList()).plus(LocalTime.now() to it)
                    )
                    externalHistoryLiveData.postValue(
                        (
                                externalHistoryLiveData.value ?: emptyList()
                                ).plus(LocalTime.now() to it)
                    )
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

            connection!!.connectToDevice(device)
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

    fun isExerciseInProgress(): Boolean {
        return isInProgress
    }

    private fun calculateDuration(data: ExerciseUpdate?): Duration {
        if (data == null) return kotlin.time.Duration.ZERO.toJavaDuration()
        val state = data.exerciseStateInfo.state
        val checkpoint = data.activeDurationCheckpoint
        val delta = if (state == ExerciseState.ACTIVE) {
            System.currentTimeMillis() - checkpoint?.time!!.toEpochMilli()
        } else {
            0L
        }
        return checkpoint!!.activeDuration.plusMillis(delta)
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
            if (heartRateSourceData.value == HeartRateSource.Device) {
                val heartRate =
                    update.latestMetrics.getData(DataType.HEART_RATE_BPM)
                        ?.lastOrNull()?.value?.toInt()
                if (heartRate != null) heartRateLiveData.postValue(heartRate!!)
                heartRateSourceData.postValue(HeartRateSource.Device)
                heartRateHistoryLiveData.postValue(
                    update.latestMetrics.getData(DataType.HEART_RATE_BPM).map {
                        LocalTime.now() to it.value.toInt()
                    }.plus(
                        heartRateHistoryLiveData.value ?: emptyList()
                    )
                )
            }

            deviceHrHistoryLiveData.postValue(
                update.latestMetrics.getData(DataType.HEART_RATE_BPM).map {
                    LocalTime.now() to it.value.toInt()
                }.plus(
                    deviceHrHistoryLiveData.value ?: emptyList()
                )
            )

            val _calories = update.latestMetrics.getData(DataType.CALORIES)
            if (_calories.isNotEmpty()) {
                val calories = _calories.sumOf { it.value }
                caloriesLiveData.postValue((caloriesLiveData.value ?: 0.0).plus(calories))
            }

            val _distance = update.latestMetrics.getData(DataType.DISTANCE)
            if (_distance.isNotEmpty()) {
                val distance = _distance.sumOf { it.value }
                distanceLiveData.postValue((distanceLiveData.value ?: 0.0).plus(distance))
            }

            val _steps = update.latestMetrics.getData(DataType.STEPS)
            if (_steps.isNotEmpty()) {
                val steps = _steps.sumOf { it.value }
                stepsLiveData.postValue((stepsLiveData.value ?: 0L).plus(steps))
            }

            val duration = calculateDuration(update)
            durationLiveData.postValue(duration)

            rawData.postValue(update)
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}

        override fun onRegistered() {}

        override fun onRegistrationFailed(throwable: Throwable) {}
    }

    // Expose LiveData for external observers
    fun getHeartRateLiveData(): LiveData<Int> = heartRateLiveData
    fun getCaloriesLiveData(): LiveData<Double> = caloriesLiveData
    fun getDistanceLiveData(): LiveData<Double> = distanceLiveData
    fun getDurationLiveData(): LiveData<Duration> = durationLiveData
    fun getRawDataLiveData(): LiveData<ExerciseUpdate?> = rawData
    fun getHeartRateSourceData(): LiveData<HeartRateSource> = heartRateSourceData
    fun getAvailability(): LiveData<Map<DataType<*, *>, Availability>> = availabilities
    fun getStepsLiveData(): LiveData<Long> = stepsLiveData
    fun getHeartRateHistoryLiveData(): LiveData<List<Pair<LocalTime, Int>>> =
        heartRateHistoryLiveData
    fun getSunlightLiveData(): LiveData<Int> = sunlightLiveData

    fun getDeviceHrHistoryLiveData(): LiveData<List<Pair<LocalTime, Int>>> = deviceHrHistoryLiveData
    fun getExternalHistoryLiveData(): LiveData<List<Pair<LocalTime, Int>>> = externalHistoryLiveData

    private fun startForegroundService(exercise: Exercise) {
        // Make an intent that will take the user straight to the exercise UI.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        HealthNotifications().createExerciseChannel(this)

        // Build the notification.
        val notificationBuilder =
            NotificationCompat.Builder(
                applicationContext,
                HealthNotifications.EXERCISE_NOTIFICATION_CHANNEL
            )
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_TEXT)
                .setSmallIcon(
                    exercise.icon
                )
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        //val startMillis = SystemClock.elapsedRealtime() - duration.toMillis()
        val ongoingActivityStatus = Status.Builder()
            .addTemplate(ONGOING_STATUS_TEMPLATE)
            .addPart("duration", Status.StopwatchPart(zeroStartTime ?: 0L))
            .build()
        val ongoingActivity =
            OngoingActivity.Builder(
                applicationContext,
                NOTIFICATION_ID, notificationBuilder
            )
                .setAnimatedIcon(exercise.icon)
                .setStaticIcon(exercise.icon)
                .setTouchIntent(pendingIntent)
                .setStatus(ongoingActivityStatus)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .build()

        ongoingActivity.apply(applicationContext)

        startForeground(1, notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        connection?.disconnect()
        exerciseClient?.clearUpdateCallbackAsync(exerciseUpdateListener)
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
