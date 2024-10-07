package com.turtlepaw.health.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "sunlight_day")
data class SunlightDay(
    //@PrimaryKey(autoGenerate = true) val id: Int = 0,
    @PrimaryKey val timestamp: LocalDate,
    val value: Int
)

@Entity(tableName = "sleep_day")
data class SleepDay(
    // val id: Int = 0,
    @PrimaryKey val bedtime: LocalDateTime,
    val asleepAt: LocalDateTime?,
    val wakeup: LocalDateTime?,
    val type: BedtimeSensor,
    //val value: Int
)

@Entity(tableName = "reflection")
data class Reflection(
    @PrimaryKey val date: LocalDateTime,
    val value: ReflectionType
)

@Entity(tableName = "services")
data class Service(
    @PrimaryKey val name: String,  // "sleep", "sunlight", etc.
    val isEnabled: Boolean
)

@Entity(tableName = "coaching_program")
data class CoachingProgram(
    @PrimaryKey val name: String,
    val progress: Int,
    val items: Map<String, Boolean>
)

enum class ServiceType(val serviceName: String) {
    SLEEP("sleep"),
    SUNLIGHT("sunlight"),
    // Add more services here
}

enum class ReflectionType(val displayName: String) {
    Calm("Calm"),
    Stressed("Stressed"),
    Excited("Excited"),
    Content("Content"),
    Worried("Worried"),
    Frustrated("Frustrated"),
    Sad("Sad"),
}

enum class CoachingType(val displayName: String) {
    Sleep("Sleep Coaching"),
}