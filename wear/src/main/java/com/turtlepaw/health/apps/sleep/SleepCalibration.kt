package com.turtlepaw.health.apps.sleep

import android.content.Context
import androidx.core.content.edit
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.sleep.SleepSession
import com.turtlepaw.shared.getDefaultSharedSettings
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class SleepCalibration(val context: Context) {
    companion object {
        private const val DEFAULT_MOTION_THRESHOLD = 0.3
        private const val DEFAULT_HEART_RATE_SLEEP_THRESHOLD = 7.0
    }

    enum class CalibrationState { NOT_STARTED, IN_PROGRESS, COMPLETED }

    var calibrationState: CalibrationState = CalibrationState.NOT_STARTED
    var motionThreshold: Double = DEFAULT_MOTION_THRESHOLD
    var heartRateThreshold: Double = DEFAULT_HEART_RATE_SLEEP_THRESHOLD
    var restingHeartRate: Double? = null

    init {
        val sharedSettings = context.getDefaultSharedSettings()
        if (sharedSettings.getBoolean("calibrated", false)) {
            calibrationState = CalibrationState.COMPLETED
        } else {
            // Check how many sessions are available
            val sessionCount = runBlocking {
                AppDatabase.getDatabase(context).sleepDao().getAllSessions().firstOrNull()?.size
                    ?: 0
            }
            calibrationState = when {
                sessionCount >= 3 -> CalibrationState.COMPLETED
                sessionCount > 0 -> CalibrationState.IN_PROGRESS
                else -> CalibrationState.NOT_STARTED
            }
        }
    }

    suspend fun calculateBaselines() {
        val database = AppDatabase.getDatabase(context)
        val sessions =
            database.sleepDao().getAllSessions().firstOrNull() ?: emptyList<SleepSession>()

        if (sessions.isEmpty()) {
            calibrationState = CalibrationState.NOT_STARTED
            return
        }

        // Use available sessions. If we have 3 or more, use the last three nights; otherwise, use what is available.
        val sessionsToUse = if (sessions.size >= 3) sessions.take(3) else sessions

        val allHeartRates = mutableListOf<Double>()
        val allMotions = mutableListOf<Double>()

        for (session in sessionsToUse) {
            val dataPoints =
                database.sleepDataPointDao().getSessionDataPoints(session.id).firstOrNull()
                    ?: continue
            // Consider only plausible heart rate values (> 30.0)
            allHeartRates.addAll(dataPoints.mapNotNull { it.heartRate?.takeIf { hr -> hr > 30.0 } })
            allMotions.addAll(dataPoints.map { it.motion })
        }

        if (allHeartRates.isNotEmpty() && allMotions.isNotEmpty()) {
            // Calculate the resting heart rate using the quietest 25% of heart rate readings
            allHeartRates.sort()
            restingHeartRate = allHeartRates.subList(0, allHeartRates.size / 4).average()

            // Calculate the motion threshold as the 75th percentile plus a 20% buffer
            allMotions.sort()
            motionThreshold = allMotions[allMotions.size * 3 / 4] * 1.2

            // Save the derived baselines
            val sharedSettings = context.getDefaultSharedSettings()
            sharedSettings.edit {
                putFloat("resting_hr", restingHeartRate?.toFloat() ?: 0f)
                putFloat("motion_threshold", motionThreshold.toFloat())

                // Set fully calibrated only if three or more sessions are available
                if (sessions.size >= 3) {
                    putBoolean("calibrated", true)
                    calibrationState = CalibrationState.COMPLETED
                } else {
                    calibrationState = CalibrationState.IN_PROGRESS
                }
            }
        }
    }
}