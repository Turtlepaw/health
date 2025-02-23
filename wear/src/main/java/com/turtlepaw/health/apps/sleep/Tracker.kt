package com.turtlepaw.health.apps.sleep

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorDirectChannel
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.exercise.manager.ExerciseService
import com.turtlepaw.health.apps.sleep.presentation.MainActivity
import com.turtlepaw.health.utils.HealthNotifications
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.sleep.SleepDataPointEntity
import com.turtlepaw.shared.database.sleep.SleepSession
import com.turtlepaw.shared.getDefaultSharedSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

enum class SleepTrackerHints {
    MotionLow,
    HeartRateLow,
}

class SleepTrackerService : LifecycleService(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var isTracking = MutableLiveData(false)
    private var isSleeping = MutableLiveData(false)
    private var isPaused = MutableLiveData(false)
    private var hints = MutableLiveData<Map<SleepTrackerHints, Double>>(emptyMap())

    private var sleepData = SleepData()
    private var lastAccelValues = DoubleArray(3)
    private var hasAccelData = false

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var trackingJob: Job? = null

    // Smart alarm configuration
    private var smartAlarmTime: LocalDateTime? = null
    private lateinit var sleepCalibration: SleepCalibration


    // Sleep detection thresholds
    companion object {
        private const val NOTIFICATION_ID = 10

        // Sleep detection constants
        private const val MOTION_THRESHOLD = 0.3
        private const val MOTION_WINDOW_MINUTES = 10
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sleepCalibration = SleepCalibration(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            "START" -> startTracking()
            "STOP" -> stopTracking()
            "PAUSE" -> pauseTracking()
            "RESUME" -> resumeTracking()
        }

        return START_STICKY
    }

    private fun startTracking() {
        if (isTracking.value == true) return

        // Reset data for new tracking session
        sleepData = SleepData()
        hasAccelData = false
        lastAccelValues = DoubleArray(3) { 0.0 }

        isTracking.value = true
        isPaused.value = false
        SleepTrackerState.run {
            updateTrackingState(true)
            updatePausedState(true)
        }

        if (sleepCalibration.calibrationState == SleepCalibration.CalibrationState.NOT_STARTED) {
            serviceScope.launch {
                sleepCalibration.calculateBaselines()
            }
        }

        // Register sensors
        registerSensors()

        // Start periodic data collection
        startPeriodicDataCollection()

        // Start foreground service
        updateNotification(
            "Tracking sleep"
        )
    }

    private fun registerSensors() {
        // Get sensors
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw IllegalStateException("No accelerometer found on device")

        val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        // Register accelerometer (required)
        val accelerometerRegistered = sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorDirectChannel.TYPE_HARDWARE_BUFFER
        )

        if (!accelerometerRegistered) {
            throw IllegalStateException("Failed to register accelerometer")
        }

        // Register heart rate sensor (optional)
        if (heartRateSensor != null && hasBodySensorPermission()) {
            sensorManager.registerListener(
                this,
                heartRateSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun stopTracking() {
        isTracking.value = false
        isPaused.value = false

        // Cancel the tracking coroutine
        trackingJob?.cancel()

        // Unregister sensors
        sensorManager.unregisterListener(this)

        // Save data if needed
        saveSleepData()

        SleepTrackerState.run {
            updateTrackingState(false)
            updatePausedState(false)
        }

        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun pauseTracking() {
        isPaused.value = true
        sensorManager.unregisterListener(this)
        updateNotification("Sleep tracking paused")
        SleepTrackerState.run {
            updateTrackingState(true)
            updatePausedState(true)
        }
    }

    private fun resumeTracking() {
        isPaused.value = false
        registerSensors()
        updateNotification("Sleep tracking active")
        SleepTrackerState.run {
            updateTrackingState(true)
            updatePausedState(false)
        }
    }

    private fun updateNotification(message: String) {
        // Make an intent that will take the user straight to the exercise UI.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        HealthNotifications().createSleepChannel(this)

        // Build the notification.
        val notificationBuilder =
            NotificationCompat.Builder(
                applicationContext,
                HealthNotifications.SLEEP_NOTIFICATION_CHANNEL
            )
                .setContentTitle("Sleep Tracking")
                .setContentText(message)
                .setSmallIcon(
                    R.drawable.sleep_white
                )
                .setContentIntent(pendingIntent)
                .addAction(
                    R.drawable.sleep_white, "Open App", pendingIntent
                )
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationManager.IMPORTANCE_LOW)

        //val startMillis = SystemClock.elapsedRealtime() - duration.toMillis()
        val ongoingActivityStatus = Status.Builder()
            .addTemplate("Tracking Sleep")
            .build()
        val ongoingActivity = OngoingActivity.Builder(
            applicationContext,
            ExerciseService.Companion.NOTIFICATION_ID, notificationBuilder
        )
            .setStaticIcon(
                R.drawable.sleep_white
            )
            .setTouchIntent(pendingIntent)
            .setStatus(ongoingActivityStatus)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        ongoingActivity.apply(applicationContext)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun hasBodySensorPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCurrentHeartRate(): Double? {
        return sleepData.getCurrentHeartRate()
    }

    private fun detectSleeping(currentMotion: Double, currentHeartRate: Double?): Boolean {
        // Use calibrated thresholds if available
        val prefs = getDefaultSharedSettings()
        val motionThreshold =
            prefs.getFloat("motion_threshold", MOTION_THRESHOLD.toFloat()).toDouble()
        val restingHr = prefs.getFloat("resting_hr", 0f).toDouble()

        val recentMotion = sleepData.getRecentMotion(MOTION_WINDOW_MINUTES)
        val avgMotion = recentMotion.map { it.motion }.average()

        val isMotionLow = avgMotion < motionThreshold

        if (isMotionLow) addHint(
            SleepTrackerHints.MotionLow, avgMotion
        )
        else removeHint(SleepTrackerHints.MotionLow)

        val isHrLow = currentHeartRate?.let { it < restingHr } == true

        if (isHrLow) addHint(SleepTrackerHints.HeartRateLow, currentHeartRate)
        else removeHint(SleepTrackerHints.HeartRateLow)

        return when {
            sleepCalibration.calibrationState == SleepCalibration.CalibrationState.COMPLETED -> isMotionLow && isHrLow
            else -> avgMotion < MOTION_THRESHOLD // Fallback to default thresholds
        }
    }

    private fun addHint(hint: SleepTrackerHints, value: Double) {
        hints.postValue(hints.value?.plus(hint to value))
    }

    private fun removeHint(hint: SleepTrackerHints) {
        hints.postValue(hints.value?.filterNot { it.key == hint } ?: emptyMap())
    }

    private fun checkSmartAlarm() {
        val now = LocalDateTime.now()
        smartAlarmTime?.let { alarmTime ->
            if (now >= alarmTime) {
                if (!isSleeping.value!!) {
                    // User is in light sleep or awake, trigger alarm
                    triggerAlarm()
                }
            }
        }
    }

    private fun triggerAlarm() {
        // Create and start alarm activity
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(alarmIntent)

        // Stop tracking
        stopTracking()
    }

    private fun saveSleepData() {
        serviceScope.launch(Dispatchers.IO) {
            val averageMotion = sleepData.getDataPoints()
                .map { it.motion }
                .average()

            val sessionId = UUID.randomUUID().toString()
            val dataPoints = sleepData.getDataPoints()
            val totalSleepMinutes = dataPoints
                .filter { it.isSleeping }
                .size

            AppDatabase.getDatabase(this@SleepTrackerService).run {
                sleepDao().insertSession(
                    SleepSession(
                        id = sessionId,
                        startTime = dataPoints.first().timestamp,
                        endTime = dataPoints.lastOrNull()?.timestamp,
                        totalSleepMinutes = totalSleepMinutes,
                        baselineHeartRate = sleepData.getBaselineHeartRate(),
                        averageMotion = averageMotion,
                        synced = false
                    )
                )

                sleepDataPointDao().insertDataPoints(
                    sleepData.getDataPoints().map {
                        SleepDataPointEntity(
                            sessionId = sessionId,
                            timestamp = it.timestamp,
                            motion = it.motion,
                            heartRate = it.heartRate,
                            isSleeping = it.isSleeping,
                            synced = false
                        )
                    }
                )
            }
        }
    }

    private fun startPeriodicDataCollection() {
        trackingJob?.cancel() // Cancel any existing job

        trackingJob = serviceScope.launch {
            while (isActive) {
                if (!isPaused.value!!) {
                    withContext(Dispatchers.Default) {
                        processCurrentData()

                        // Update baseline readings if needed
                        updateBaselineReadings()

                        // Check for sleep state changes
                        val newSleepState = detectSleeping(
                            currentMotion = calculateMotion(),
                            currentHeartRate = getCurrentHeartRate()
                        )

                        if (newSleepState != isSleeping.value) {
                            isSleeping.postValue(newSleepState)
                            withContext(Dispatchers.Main) {
                                updateNotification(
                                    if (isSleeping.value!!) "You're sleeping"
                                    else "Monitoring sleep"
                                )
                            }
                        }

                        // Check smart alarm conditions
                        checkSmartAlarm()
                    }
                }
                delay(60_000) // Collect data every minute
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isTracking.value!! || isPaused.value!!) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // Calculate acceleration change from last reading
                var motionValue = 0.0
                for (i in event.values.indices) {
                    val delta = event.values[i] - lastAccelValues[i]
                    motionValue += delta * delta
                    lastAccelValues[i] = event.values[i].toDouble()
                }

                // Store the motion value
                sleepData.addMotionSample(motionValue)
                hasAccelData = true
            }

            Sensor.TYPE_HEART_RATE -> {
                val heartRate = event.values[0].toDouble()
                if (heartRate > 0) { // Filter out invalid readings
                    sleepData.updateHeartRate(heartRate)
                }
            }
        }
    }

    private fun updateBaselineReadings() {
        // Update baseline readings during first 10 minutes of tracking
        if (sleepData.getDataPoints().size < 10) {
            val recentMotion = sleepData.getRecentMotion(1) // Last minute
            val avgMotion = recentMotion.map { it.motion }.average()
            sleepData.updateBaselineMotion(avgMotion)

            getCurrentHeartRate()?.let { hr ->
                sleepData.updateBaselineHeartRate(hr)
            }
        }
    }

    private fun calculateMotion(): Double {
        // Process any pending motion samples first
        sleepData.processMotionSamples()

        // Get recent motion data (last minute)
        val recentMotion = sleepData.getRecentMotion(1)

        // If we have recent motion data, return the latest value
        // otherwise return 0.0 as a default
        return if (recentMotion.isNotEmpty()) {
            recentMotion.last().motion
        } else {
            0.0
        }
    }

    private fun processCurrentData() {
        // Process motion samples and add new data point
        sleepData.processMotionSamples()

        // Get the current motion value
        val currentMotion = calculateMotion()

        // Create a new data point with current values
        val newDataPoint = SleepDataPoint(
            timestamp = LocalDateTime.now(),
            motion = currentMotion,
            heartRate = getCurrentHeartRate(),
            isSleeping = isSleeping.value!! // Use the current sleep state
        )

        // Add the new data point to our sleep data
        sleepData.addDataPoint(newDataPoint)
    }

    // Required but unused SensorEventListener method
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this implementation
    }

    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): SleepTrackerService = this@SleepTrackerService
    }

    fun getTrackingState() = isTracking
    fun getPausedState() = isPaused
    fun getHints() = hints
    fun getSleepState() = isSleeping
}

// Extended data classes
data class SleepDataPoint(
    val timestamp: LocalDateTime,
    val motion: Double,
    val heartRate: Double?,
    val isSleeping: Boolean
)

class SleepData {
    private val dataPoints = mutableListOf<SleepDataPoint>()
    private var currentHeartRate: Double? = null
    private var baselineHeartRate: Double? = null
    private var baselineMotion: Double? = null

    private val motionSamples = mutableListOf<Double>()
    private var lastProcessedTime = LocalDateTime.now()

    fun addMotionSample(motion: Double) {
        motionSamples.add(motion)
    }

    fun processMotionSamples() {
        if (motionSamples.isNotEmpty()) {
            val averageMotion = motionSamples.average()
            addDataPoint(
                SleepDataPoint(
                    timestamp = lastProcessedTime,
                    motion = averageMotion,
                    heartRate = currentHeartRate,
                    isSleeping = false // Will be updated by sleep detection
                )
            )
            motionSamples.clear()
            lastProcessedTime = LocalDateTime.now()
        }
    }

    fun updateBaselineMotion(motion: Double) {
        if (baselineMotion == null) {
            baselineMotion = motion
        } else {
            baselineMotion = (baselineMotion!! * 0.8 + motion * 0.2) // Weighted average
        }
    }

    fun updateBaselineHeartRate(hr: Double) {
        if (baselineHeartRate == null) {
            baselineHeartRate = hr
        } else {
            baselineHeartRate = (baselineHeartRate!! * 0.8 + hr * 0.2) // Weighted average
        }
    }

    fun getDataPoints(): List<SleepDataPoint> = dataPoints.toList()

    fun addDataPoint(point: SleepDataPoint) {
        dataPoints.add(point)

        // Calculate baseline heart rate from first 10 minutes if not set
        if (baselineHeartRate == null && dataPoints.size >= 10) {
            baselineHeartRate = dataPoints
                .take(10)
                .mapNotNull { it.heartRate }
                .average()
        }
    }

    fun updateHeartRate(hr: Double) {
        currentHeartRate = hr
    }

    fun getCurrentHeartRate() = currentHeartRate

    fun getBaselineHeartRate() = baselineHeartRate

    fun getRecentMotion(minutes: Int): List<SleepDataPoint> {
        val cutoff = LocalDateTime.now().minusMinutes(minutes.toLong())
        return dataPoints.filter { it.timestamp.isAfter(cutoff) }
    }
}