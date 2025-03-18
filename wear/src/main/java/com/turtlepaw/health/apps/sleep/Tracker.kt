package com.turtlepaw.health.apps.sleep

import android.Manifest
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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.exercise.manager.ExerciseService
import com.turtlepaw.health.apps.sleep.presentation.MainActivity
import com.turtlepaw.health.apps.sleep.utils.Settings
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
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.pow

enum class SleepTrackerHints {
    MotionLow,
    HeartRateLow,
    LightSleepDetected
}

class SleepTrackerService : LifecycleService(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var isTracking = MutableLiveData(false)
    private var isSleeping = MutableLiveData(false)
    private var isPaused = MutableLiveData(false)
    private var activeHints = MutableLiveData(mapOf<SleepTrackerHints, Any>())

    private var sleepData = SleepData()
    private var lastAccelValues = DoubleArray(3)
    private var hasAccelData = false

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var trackingJob: Job? = null

    // Smart alarm configuration
    private var smartAlarmTime: LocalTime? = null
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

        smartAlarmTime = getDefaultSharedSettings().getString(Settings.ALARM.getKey(), null).run {
            if (this == null) null
            else try {
                LocalTime.parse(this)
            } catch (e: Exception) {
                Log.e("AlarmSettings", "Error parsing alarm time: $this", e)
                null
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
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

        // Auto-start calibration if needed
        if (sleepCalibration.calibrationState == SleepCalibration.CalibrationState.LEARNING ||
            sleepCalibration.calibrationState == SleepCalibration.CalibrationState.NEEDS_REFRESH
        ) {
            serviceScope.launch {
                sleepCalibration.calculateBaselines()
            }
        }

        // Register sensors
        registerSensors()

        // Start periodic data collection
        startPeriodicDataCollection()

        // Update notification with calibration status
        val statusMessage = when (sleepCalibration.calibrationState) {
            SleepCalibration.CalibrationState.LEARNING -> "Learning your patterns"
            SleepCalibration.CalibrationState.STABLE -> "Sleep tracking active"
            SleepCalibration.CalibrationState.NEEDS_REFRESH -> "Needs recalibration"
        }
        updateNotification(statusMessage)
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
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

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

        // Use night HR for sleep detection instead of generic "resting_hr"
        val restingHr = prefs.getFloat("resting_hr_night", 0f).toDouble()

        val recentMotion = sleepData.getRecentMotion(MOTION_WINDOW_MINUTES)
        val avgMotion = recentMotion.map { it.motion }.average()

        val isMotionLow = avgMotion < motionThreshold

        if (isMotionLow) addHint(
            SleepTrackerHints.MotionLow, avgMotion
        )
        else removeHint(SleepTrackerHints.MotionLow)

        val isHrLow = currentHeartRate?.let { it < restingHr } == true

        if (isHrLow) addHint(SleepTrackerHints.HeartRateLow, currentHeartRate ?: 0.0)
        else removeHint(SleepTrackerHints.HeartRateLow)

        return when {
            // Change COMPLETED to STABLE to match the enum in SleepCalibration
            sleepCalibration.calibrationState == SleepCalibration.CalibrationState.STABLE -> isMotionLow && isHrLow
            else -> avgMotion < MOTION_THRESHOLD // Fallback to default thresholds
        }
    }

    private fun detectLightSleepPhase(): Boolean {
        val recentData = sleepData.getRecentMotion(5)
        val hrTrend = recentData.heartRateTrend()
        val motionVariance = recentData.motionVariance()

        val isLightSleep = hrTrend between (0.88..0.95) &&
                motionVariance between (0.02..0.15)

        if (isLightSleep) {
            addHint(SleepTrackerHints.LightSleepDetected, hrTrend to motionVariance)
        } else {
            removeHint(SleepTrackerHints.LightSleepDetected)
        }

        return isLightSleep
    }

    private fun addHint(hint: SleepTrackerHints, value: Any) {
        activeHints.postValue(
            activeHints.value?.plus(hint to value)
        )
    }

    private fun removeHint(hint: SleepTrackerHints) {
        activeHints.postValue(
            activeHints.value?.minus(hint)
        )
    }

    fun getActiveHints(): MutableLiveData<Map<SleepTrackerHints, Any>> = activeHints

    // Function to process new sensor data
    fun processSensorData(motion: Double, heartRate: Double?) {
        // Update data
        sleepData.addMotionSample(motion)
        heartRate?.let { sleepData.updateHeartRate(it) }

        // Process accumulated samples
        sleepData.processMotionSamples()

        // Run detection algorithms
        val isSleeping = detectSleeping(motion, heartRate)
        val isLightSleep = if (isSleeping) detectLightSleepPhase() else false

        // Update the latest data point with sleep state
        val dataPoints = sleepData.getDataPoints()
        if (dataPoints.isNotEmpty()) {
            dataPoints.last().isSleeping = isSleeping
        }
    }

    // Function to get sleep quality report
    fun getSleepQualityReport(): Map<String, Any> {
        val dataPoints = sleepData.getDataPoints()
        val sleepPoints = dataPoints.filter { it.isSleeping }

        if (sleepPoints.isEmpty()) {
            return mapOf("status" to "No sleep detected")
        }

        val totalSleepMinutes = sleepPoints.size // Assuming one data point per minute
        val avgHeartRate = sleepPoints.mapNotNull { it.heartRate }.average()
        val avgMotion = sleepPoints.map { it.motion }.average()

        return mapOf(
            "totalSleepMinutes" to totalSleepMinutes,
            "averageHeartRate" to avgHeartRate,
            "averageMotion" to avgMotion,
            "calibrationState" to sleepCalibration.calibrationState.name
        )
    }

    // Function to handle user feedback on wake quality
    fun provideWakeFeedback(feelingGood: Boolean) {
        sleepCalibration.handleWakeFeedback(feelingGood)
    }

    // Function to initiate calibration
    suspend fun calibrateSleepAlgorithm() {
        sleepCalibration.calculateBaselines()
    }

    private fun triggerAlarm() {
        // Create and start alarm activity
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(alarmIntent)

        //        startActivity(Intent(this, WakeFeedbackActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        })
    }

    /**
     * Checks if the smart alarm should be triggered and handles it accordingly
     *
     * For a 10am alarm:
     * - 10:01am → alarm should trigger (within 15min after alarm time)
     * - 10:00am → alarm should trigger (exact alarm time)
     * - 11:00pm → alarm should NOT trigger (outside of alarm window)
     */
    private fun checkSmartAlarm() {
        val alarmTime = smartAlarmTime ?: return
        val now = LocalTime.now()
        val currentDateTime = LocalDateTime.now()

        // Calculate if current time is within the wake window
        // For a 10am alarm, wake window is 9:30am to 10:15am
        val inWakeWindow = now.isAfter(alarmTime.minusMinutes(30)) &&
                now.isBefore(alarmTime.plusMinutes(15))

        // For a 10am alarm:
        // - At 10:01am, this is true (within 15min after alarm time)
        // - At 10:00am, this is true (exact alarm time)
        // - At 11:00pm, this is false (after alarm time but not within 15min window)
        val isAlarmTriggerable = now.equals(alarmTime) ||
                (now.isAfter(alarmTime) && now.isBefore(alarmTime.plusMinutes(15)))

        // For a 10am alarm, this should be:
        // - At 10:01am → trigger alarm
        // - At 10:00am → trigger alarm
        // - At 11:00pm → do not trigger alarm
        if (isAlarmTriggerable || (inWakeWindow && isOptimalWakeTime())) {
            triggerAlarm()
        }
    }

    private fun isOptimalWakeTime(): Boolean {
        val prefs = getDefaultSharedSettings()
        val restingHr = prefs.getFloat("resting_hr_night", 60f).toDouble()

        return detectLightSleepPhase() &&
                hoursSlept() >= 3 &&
                isLowMovementVariance()
    }

    private fun hoursSlept(): Long {
        return sleepData.getDataPoints().firstOrNull()?.let {
            ChronoUnit.HOURS.between(it.timestamp, LocalDateTime.now())
        } ?: 0
    }

    private fun isLowMovementVariance(): Boolean {
        val motions = sleepData.getRecentMotion(30).map { it.motion }
        val variance = calculateVariance(motions)
        return variance < 0.1
    }

    private fun calculateVariance(data: List<Double>): Double {
        val mean = data.average()
        return data.map { (it - mean).pow(2) }.average()
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
    fun getHints() = activeHints
    fun getSleepState() = isSleeping
}

// Extension function for range checks
infix fun <T : Comparable<T>> T.between(range: ClosedRange<T>): Boolean {
    return this in range
}

// Extension functions for lists of SleepDataPoint
fun List<SleepDataPoint>.heartRateTrend(): Double {
    if (this.size < 3) return 1.0
    val hrValues = this.mapNotNull { it.heartRate }
    if (hrValues.size < 3) return 1.0

    // Calculate the trend as ratio of latest HR to average of previous values
    val latestHr = hrValues.last()
    val previousAvg = hrValues.dropLast(1).average()
    return if (previousAvg > 0) latestHr / previousAvg else 1.0
}

fun List<SleepDataPoint>.motionVariance(): Double {
    if (this.size < 3) return 0.0
    val motionValues = this.map { it.motion }
    val mean = motionValues.average()
    val variance = motionValues.map { (it - mean).pow(2) }.average()
    return variance
}

// Extended data classes
data class SleepDataPoint(
    val timestamp: LocalDateTime,
    val motion: Double,
    val heartRate: Double?,
    var isSleeping: Boolean
)

