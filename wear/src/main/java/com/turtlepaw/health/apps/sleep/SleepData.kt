package com.turtlepaw.health.apps.sleep

import java.time.LocalDateTime

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