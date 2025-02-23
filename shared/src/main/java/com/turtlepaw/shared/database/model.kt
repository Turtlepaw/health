package com.turtlepaw.shared.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "sleep_day")
data class SleepDay(
    // val id: Int = 0,
    @PrimaryKey val bedtime: LocalDateTime,
    val asleepAt: LocalDateTime?,
    val wakeup: LocalDateTime?,
    val type: BedtimeSensor,
    //val value: Int
)

@Entity(tableName = "coaching_program")
data class CoachingProgram(
    @PrimaryKey val name: String,
    val progress: Int,
    val items: Map<String, Boolean>
)

@Entity(tableName = "day")
data class Day(
    @PrimaryKey val date: LocalDate,
    val steps: Int,
    val goal: Int
)

enum class CoachingType(val displayName: String) {
    Sleep("Sleep Coaching"),
}