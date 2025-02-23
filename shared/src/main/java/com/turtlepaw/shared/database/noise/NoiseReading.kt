package com.turtlepaw.shared.database.noise

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turtlepaw.shared.database.SyncableEntity
import java.time.LocalDateTime

@Entity(tableName = "noise_reading")
data class NoiseReading(
    @PrimaryKey val date: LocalDateTime,
    val value: Double,
    override val synced: Boolean
) : SyncableEntity