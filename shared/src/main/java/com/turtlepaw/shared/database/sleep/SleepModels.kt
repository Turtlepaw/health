package com.turtlepaw.shared.database.sleep

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turtlepaw.shared.database.SyncableEntity
import java.time.LocalDateTime

@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey val id: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val totalSleepMinutes: Int? = null,
    val baselineHeartRate: Double? = null,
    val averageMotion: Double? = null,
    override val synced: Boolean = false
) : SyncableEntity

@Entity(tableName = "sleep_data_points")
data class SleepDataPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val timestamp: LocalDateTime,
    val motion: Double,
    val heartRate: Double?,
    val isSleeping: Boolean,
    override val synced: Boolean = false
) : SyncableEntity