package com.turtlepaw.shared.database.sunlight

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.turtlepaw.shared.database.RoomSyncableDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
abstract class SunlightDao : RoomSyncableDao<SunlightDay, LocalDate>() {
    @Query("SELECT * FROM sunlight_day ORDER BY timestamp DESC")
    abstract suspend fun getHistory(): List<SunlightDay>

    @Query("DELETE FROM sunlight_day WHERE timestamp = :day")
    abstract suspend fun deleteDay(day: LocalDate)

    @Query("UPDATE sunlight_day SET value = :value WHERE timestamp = :day")
    abstract suspend fun updateDay(day: LocalDate, value: Int)

    @Query("SELECT * FROM sunlight_day WHERE timestamp = :day")
    abstract suspend fun getDay(day: LocalDate): SunlightDay?

    @Query("SELECT * FROM sunlight_day WHERE timestamp = :day LIMIT 1")
    abstract fun getLiveDay(day: LocalDate): Flow<SunlightDay?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDay(sunlightDay: SunlightDay)

    @Query("SELECT * FROM sunlight_day WHERE synced = 0")
    @JvmSuppressWildcards
    abstract override suspend fun getUnsyncedInternal(): List<SunlightDay>

    @Query("UPDATE sunlight_day SET synced = 1 WHERE timestamp IN (:ids)")
    @JvmSuppressWildcards
    abstract override suspend fun markSyncedInternal(ids: List<LocalDate>)
}

@Dao
abstract class SunlightGoalDao : RoomSyncableDao<SunlightDay, LocalDate>() {
    @Query("SELECT * FROM sunlight_goal LIMIT 1")
    open suspend fun getGoal(): SunlightGoal {
        return getGoalInternal() ?: SunlightGoal(
            goal = 15,
            synced = false
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertGoal(goal: SunlightGoal)

    @Query("SELECT * FROM sunlight_day WHERE synced = 0")
    @JvmSuppressWildcards
    abstract override suspend fun getUnsyncedInternal(): List<SunlightDay>

    @Query("UPDATE sunlight_day SET synced = 1 WHERE timestamp IN (:ids)")
    @JvmSuppressWildcards
    abstract override suspend fun markSyncedInternal(ids: List<LocalDate>)

    @Query("SELECT * FROM sunlight_goal LIMIT 1")
    protected abstract suspend fun getGoalInternal(): SunlightGoal?
}