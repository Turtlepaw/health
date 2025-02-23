package com.turtlepaw.shared.database.sleep

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.turtlepaw.shared.database.RoomSyncableDao
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SleepDataPointDao : RoomSyncableDao<SleepDataPointEntity, String>() {
    @Insert
    abstract suspend fun insertDataPoints(points: List<SleepDataPointEntity>)

    @Query("SELECT * FROM sleep_data_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    abstract fun getSessionDataPoints(sessionId: String): Flow<List<SleepDataPointEntity>>

    @Query("SELECT * FROM sleep_data_points WHERE synced = 0")
    @JvmSuppressWildcards
    abstract override suspend fun getUnsyncedInternal(): List<SleepDataPointEntity>

    @Query("UPDATE sleep_data_points SET synced = 1 WHERE id IN (:ids)")
    @JvmSuppressWildcards
    abstract override suspend fun markSyncedInternal(ids: List<String>)
}