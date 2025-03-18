package com.turtlepaw.shared.database.sleep

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.turtlepaw.shared.database.RoomSyncableDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
abstract class SleepSessionDao : RoomSyncableDao<SleepSession, String>() {
    @Insert
    abstract suspend fun insertSession(session: SleepSession)

    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC")
    abstract fun getAllSessions(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    abstract fun getSessionById(id: String): Flow<SleepSession?>

    @Query("SELECT * FROM sleep_sessions WHERE synced = 0")
    @JvmSuppressWildcards
    abstract override suspend fun getUnsyncedInternal(): List<SleepSession>

    @Query("UPDATE sleep_sessions SET synced = 1 WHERE id IN (:ids)")
    @JvmSuppressWildcards
    abstract override suspend fun markSyncedInternal(ids: List<String>)

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    abstract suspend fun deleteSession(id: String)

    @Query("SELECT * FROM sleep_sessions WHERE startTime > :cutoff")
    abstract suspend fun getSessionsSince(cutoff: LocalDateTime): List<SleepSession>
}