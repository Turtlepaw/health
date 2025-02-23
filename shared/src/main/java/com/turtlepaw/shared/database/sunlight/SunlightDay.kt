package com.turtlepaw.shared.database.sunlight

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turtlepaw.shared.database.SyncableEntity
import java.time.LocalDate

@Entity(tableName = "sunlight_day")
data class SunlightDay(
    //@PrimaryKey(autoGenerate = true) val id: Int = 0,
    @PrimaryKey val timestamp: LocalDate,
    val value: Int,
    override val synced: Boolean = false
) : SyncableEntity

@Entity(tableName = "sunlight_goal")
data class SunlightGoal(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val goal: Int,
    override val synced: Boolean = false
) : SyncableEntity