package com.turtlepaw.health.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.turtlepaw.health.database.exercise.DayDao
import com.turtlepaw.health.database.exercise.Exercise
import com.turtlepaw.health.database.exercise.ExerciseDao
import com.turtlepaw.health.database.exercise.Preference
import com.turtlepaw.health.database.exercise.PreferenceDao

@Database(
    entities = [SleepDay::class, SunlightDay::class, Service::class, Reflection::class, CoachingProgram::class, Preference::class, Exercise::class, Day::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sunlightDao(): SunlightDao
    abstract fun sleepDao(): SleepDao
    abstract fun serviceDao(): ServiceDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun coachingProgramDao(): CoachingProgramDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun dayDao(): DayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}