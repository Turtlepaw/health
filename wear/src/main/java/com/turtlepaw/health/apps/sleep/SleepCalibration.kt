package com.turtlepaw.health.apps.sleep

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.sleep.SleepSession
import com.turtlepaw.shared.getDefaultSharedSettings
import java.time.LocalDateTime
import kotlin.random.Random

class SleepCalibration(val context: Context) {
    companion object {
        private const val CALIBRATION_WINDOW_DAYS = 7
        private const val FEEDBACK_WEIGHT = 0.15f
        private const val MOTION_BUFFER_FACTOR = 1.15
    }

    enum class CalibrationState {
        LEARNING, STABLE, NEEDS_REFRESH
    }

    var calibrationState = CalibrationState.LEARNING

    init {
        val prefs = context.getDefaultSharedSettings()
        if (prefs.getBoolean("calibrated", false)) {
            calibrationState = if (needsRefresh()) {
                CalibrationState.NEEDS_REFRESH
            } else {
                CalibrationState.STABLE
            }
        }
    }

    suspend fun calculateBaselines() {
        val database = AppDatabase.getDatabase(context)
        val cutoff = LocalDateTime.now().minusDays(CALIBRATION_WINDOW_DAYS.toLong())

        val sessions = database.sleepDao().getSessionsSince(cutoff)
            .filter { (it.totalSleepMinutes ?: 0) >= 240 } // Only valid sleep sessions

        if (sessions.isEmpty()) {
            try {
                Toast.makeText(context, "No sleep sessions", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        val (dayHr, nightHr, motions) = collectSensorData(sessions)
        val feedbackImpact = calculateFeedbackImpact()

        updateThresholds(
            daytimeHr = calculateBaseline(dayHr),
            nighttimeHr = calculateBaseline(nightHr),
            motions = motions,
            feedbackImpact = feedbackImpact
        )

        updateCalibrationState(sessions.size)
    }

    private suspend fun collectSensorData(sessions: List<SleepSession>): Triple<List<Double>, List<Double>, List<Double>> {
        val database = AppDatabase.getDatabase(context)
        val daytimeHr = mutableListOf<Double>()
        val nighttimeHr = mutableListOf<Double>()
        val motions = mutableListOf<Double>()

        sessions.forEach { session ->
            database.sleepDataPointDao().getSessionDataPoints(session.id).collect { points ->
                points.forEach { point ->
                    when {
                        point.timestamp.hour in 10..19 -> point.heartRate?.let { daytimeHr.add(it) }
                        point.timestamp.hour in 22..23 || point.timestamp.hour in 0..5 -> {
                            point.heartRate?.let { nighttimeHr.add(it) }
                            motions.add(point.motion)
                        }
                    }
                }
            }
        }
        return Triple(daytimeHr, nighttimeHr, motions)
    }

    private fun calculateBaseline(readings: List<Double>): Double {
        return readings.sorted().let {
            val quartile = it.size / 4
            it.subList(quartile, quartile * 3).average() // Middle 50%
        }
    }

    private fun calculateFeedbackImpact(): Float {
        val prefs = context.getDefaultSharedSettings()
        val goodWakes = prefs.getInt("good_wakes", 0)
        val totalWakes = prefs.getInt("total_wakes", 1)
        return (goodWakes.toFloat() / totalWakes).coerceIn(0.2f, 0.8f)
    }

    private fun updateThresholds(
        daytimeHr: Double,
        nighttimeHr: Double,
        motions: List<Double>,
        feedbackImpact: Float
    ) {
        val prefs = context.getDefaultSharedSettings()
        val currentThreshold = prefs.getFloat("motion_threshold", 0.3f)

        val newMotionThreshold = motions.sorted().let {
            val base = it[it.size * 85 / 100] * MOTION_BUFFER_FACTOR
            (base * (1 - FEEDBACK_WEIGHT)) + (currentThreshold * FEEDBACK_WEIGHT * feedbackImpact)
        }.coerceIn(0.2, 0.5)

        prefs.edit {
            putFloat("resting_hr_day", daytimeHr.toFloat())
            putFloat("resting_hr_night", nighttimeHr.toFloat())
            putFloat("motion_threshold", newMotionThreshold.toFloat())
            putBoolean("calibrated", true)
            putLong("last_calibrated", System.currentTimeMillis())
        }
    }

    fun handleWakeFeedback(feelingGood: Boolean) {
        val prefs = context.getDefaultSharedSettings()
        prefs.edit {
            putInt("total_wakes", prefs.getInt("total_wakes", 0) + 1)
            if (feelingGood) {
                putInt("good_wakes", prefs.getInt("good_wakes", 0) + 1)
            }
        }
        if (Random.nextInt(5) == 0) calibrationState = CalibrationState.NEEDS_REFRESH
    }

    private fun needsRefresh(): Boolean {
        val lastCalibrated = context.getDefaultSharedSettings()
            .getLong("last_calibrated", 0)
        return System.currentTimeMillis() - lastCalibrated > 604_800_000 // 7 days
    }

    private fun updateCalibrationState(sessionCount: Int) {
        calibrationState = when {
            sessionCount >= CALIBRATION_WINDOW_DAYS -> CalibrationState.STABLE
            else -> CalibrationState.LEARNING
        }
    }
}
