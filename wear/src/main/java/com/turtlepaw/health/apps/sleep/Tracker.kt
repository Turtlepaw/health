package com.turtlepaw.health.apps.sleep

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class ModernSleepTrackerService : LifecycleService(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var isTracking = MutableLiveData(false)
    private var isPaused = MutableLiveData(false)

    private var sleepData = SleepData()
    private var lastAccelValues = DoubleArray(3) { 0.0 }
    private var hasAccelData = false

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var trackingJob: Job? = null

    // Smart alarm configuration
    private var smartAlarmTime: LocalDateTime? = null
    private var isSleeping = false

    // Sleep detection thresholds
    companion object {
        private const val NOTIFICATION_ID = 10
        private const val CHANNEL_ID = "sleep_tracker"
        private const val CHANNEL_NAME = "Sleep Tracking"

        // Sleep detection constants
        private const val MOTION_THRESHOLD = 0.3
        private const val HEART_RATE_SLEEP_THRESHOLD = 7.0 // BPM decrease from baseline
        private const val MOTION_WINDOW_MINUTES = 10
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        createNotificationChannel()
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

        // Register sensors
        registerSensors()

        // Start periodic data collection
        startPeriodicDataCollection()

        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification())
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
            SensorManager.SENSOR_DELAY_NORMAL
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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Used for sleep tracking notifications"
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
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

        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun pauseTracking() {
        isPaused.value = true
        sensorManager.unregisterListener(this)
        updateNotification("Sleep tracking paused")
    }

    private fun resumeTracking() {
        isPaused.value = false
        registerSensors()
        updateNotification("Sleep tracking active")
    }

    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
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
        // Get recent motion data
        val recentMotion = sleepData.getRecentMotion(MOTION_WINDOW_MINUTES)

        // Calculate average motion
        val averageMotion = recentMotion
            .map { it.motion }
            .average()

        // Check if motion is below threshold
        val isMotionLow = averageMotion < MOTION_THRESHOLD

        // Check heart rate if available
        val isHeartRateLow = if (currentHeartRate != null) {
            // Get baseline heart rate (average from first 10 minutes of tracking)
            val baselineHR = sleepData.getBaselineHeartRate()
            baselineHR?.let {
                currentHeartRate < (it - HEART_RATE_SLEEP_THRESHOLD)
            } == true
        } else {
            false
        }

        // Detect sleep based on available signals
        return if (currentHeartRate != null) {
            // If we have heart rate data, require both signals
            isMotionLow && isHeartRateLow
        } else {
            // Otherwise use just motion
            isMotionLow
        }
    }

    private fun checkSmartAlarm() {
        val now = LocalDateTime.now()
        smartAlarmTime?.let { alarmTime ->
            if (now >= alarmTime) {
                if (!isSleeping) {
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
            // Here you would save to Room database or other storage
            // For example:
            // database.sleepDao().insertSleepSession(sleepData.toSleepSession())
        }
    }

    private fun createNotification(message: String = "Monitoring your sleep patterns"): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sleep Tracking Active")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
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

                        if (newSleepState != isSleeping) {
                            isSleeping = newSleepState
                            withContext(Dispatchers.Main) {
                                updateNotification(
                                    if (isSleeping) "You're sleeping"
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
            isSleeping = isSleeping // Use the current sleep state
        )

        // Add the new data point to our sleep data
        sleepData.addDataPoint(newDataPoint)
    }

    // Required but unused SensorEventListener method
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this implementation
    }

    // Required but unused Service method
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
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