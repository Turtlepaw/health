package com.turtlepaw.shared.database.exercise

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.turtlepaw.heart_connection.Exercises
import com.turtlepaw.heart_connection.Metric
import com.turtlepaw.shared.database.Day
import com.turtlepaw.shared.database.RoomSyncableDao
import java.time.LocalDateTime

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: Preference)

    @Query("SELECT * FROM preferences WHERE id = :id")
    suspend fun getPreference(id: Int): Preference

    suspend fun changeMetric(id: Int, position: Int, newMetric: Metric) {
        val old = getPreference(id)
        insertPreference(
            Preference(
                id,
                metrics = old.metrics.mapIndexed { index, metric ->
                    if (index == position) newMetric
                    else metric
                }.take(4)
            )
        )
    }

    suspend fun getOrInsertPreference(id: Int): Preference {
        val preference = getPreference(id) as Preference?
        return if (preference == null) {
            insertPreference(
                Preference(
                    id = id,
                    metrics = Exercises.elementAt(id).defaultMetrics.take(4)
                )
            )

            getPreference(id)
        } else preference
    }
}

@Dao
abstract class ExerciseDao : RoomSyncableDao<Exercise, LocalDateTime>() {
    @Insert
    abstract suspend fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM exercise ORDER BY timestamp DESC")
    abstract suspend fun getExercises(): List<Exercise>

    @Query("SELECT * FROM exercise WHERE synced = 0")
    @JvmSuppressWildcards
    abstract override suspend fun getUnsyncedInternal(): List<Exercise>

    @Query("UPDATE exercise SET synced = 1 WHERE timestamp IN (:ids)")
    @JvmSuppressWildcards
    abstract override suspend fun markSyncedInternal(ids: List<LocalDateTime>)
}

@Dao
interface DayDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertDay(day: Day)

    @Query("SELECT * FROM day ORDER BY date DESC")
    suspend fun getDays(): List<Day>

//    @Query("DELETE FROM exercise WHERE id = :favoriteId")
//    suspend fun deleteFavoriteById(favoriteId: Int)
}

