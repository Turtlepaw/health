package com.turtlepaw.health.database.exercise

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turtlepaw.heart_connection.Metric
import java.time.LocalDateTime

@Entity(tableName = "preferences")
data class Preference(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val metrics: List<Metric>
)

@Entity(tableName = "exercise")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: LocalDateTime,
    val bpm: List<Int>,
    val type: Int
)
