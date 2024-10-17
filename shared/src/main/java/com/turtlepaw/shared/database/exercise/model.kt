package com.turtlepaw.shared.database.exercise

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turtlepaw.heart_connection.Metric
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "preferences")
data class Preference(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val metrics: List<Metric>
)

@Entity(tableName = "exercise")
data class Exercise(
    @PrimaryKey val timestamp: LocalDateTime,
    val exercise: Int,
    val averageHeartRate: Double?,
    val totalDistance: Double?,
    val totalCalories: Double?,
    val elapsedTime: Duration,
    val maxHeartRate: Int?,
    val steps: Long? = null,
    val heartRateSimilarity: Double? = null,
    val heartRateHistory: List<Pair<LocalTime, Int>>,
    val sunlight: Int
)
