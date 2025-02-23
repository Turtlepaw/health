package com.turtlepaw.shared.database.reflect

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.turtlepaw.shared.database.RoomSyncableDao
import java.time.LocalDateTime

@Dao
abstract class ReflectionDao : RoomSyncableDao<Reflection, LocalDateTime>() {
    @Insert
    abstract suspend fun insertReflection(day: Reflection)

    // get latest
    @Query("SELECT * FROM reflection ORDER BY date DESC LIMIT 1")
    abstract suspend fun getLatest(): Reflection?

    @Query("SELECT * FROM reflection ORDER BY date DESC")
    abstract suspend fun getHistory(): List<Reflection>

    @Query("DELETE FROM reflection")
    abstract suspend fun deleteAll()

    @Query("SELECT * FROM reflection WHERE synced = 0")
    @JvmSuppressWildcards
    abstract override suspend fun getUnsyncedInternal(): List<Reflection>

    @Query("UPDATE reflection SET synced = 1 WHERE date IN (:ids)")
    @JvmSuppressWildcards
    abstract override suspend fun markSyncedInternal(ids: List<LocalDateTime>)
}