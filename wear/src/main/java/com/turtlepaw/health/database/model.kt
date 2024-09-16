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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bedtime: LocalDateTime,
    val asleepAt: LocalDateTime,
    val wakeup: LocalDateTime,
    val type: BedtimeSensor,
    val value: Int
)
