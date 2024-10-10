package com.turtlepaw.health.database.exercise

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.turtlepaw.health.database.Day
import com.turtlepaw.heart_connection.Exercises
import com.turtlepaw.heart_connection.Metric

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
interface ExerciseDao {
    @Insert
    suspend fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM exercise ORDER BY timestamp DESC")
    suspend fun getExercises(): List<Exercise>

//    @Query("DELETE FROM exercise WHERE id = :favoriteId")
//    suspend fun deleteFavoriteById(favoriteId: Int)
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

