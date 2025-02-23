package com.turtlepaw.shared.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.turtlepaw.shared.database.exercise.DayDao
import com.turtlepaw.shared.database.exercise.Exercise
import com.turtlepaw.shared.database.exercise.ExerciseDao
import com.turtlepaw.shared.database.exercise.Preference
import com.turtlepaw.shared.database.exercise.PreferenceDao
import com.turtlepaw.shared.database.reflect.Reflection
import com.turtlepaw.shared.database.reflect.ReflectionDao
import com.turtlepaw.shared.database.services.Service
import com.turtlepaw.shared.database.services.ServiceDao
import com.turtlepaw.shared.database.sleep.SleepDataPointDao
import com.turtlepaw.shared.database.sleep.SleepDataPointEntity
import com.turtlepaw.shared.database.sleep.SleepSession
import com.turtlepaw.shared.database.sleep.SleepSessionDao
import com.turtlepaw.shared.database.sunlight.SunlightDao
import com.turtlepaw.shared.database.sunlight.SunlightDay
import com.turtlepaw.shared.database.sunlight.SunlightGoal

@Database(
    entities = [
        SleepDataPointEntity::class,
        SleepSession::class,
        SunlightDay::class,
        Service::class,
        Reflection::class,
        CoachingProgram::class,
        Preference::class,
        Exercise::class,
        Day::class,
        SunlightGoal::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sunlightDao(): SunlightDao
    abstract fun sleepDao(): SleepSessionDao
    abstract fun sleepDataPointDao(): SleepDataPointDao
    abstract fun serviceDao(): ServiceDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun coachingProgramDao(): CoachingProgramDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun dayDao(): DayDao

    companion object {
        private const val TAG = "AppDatabase"
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val LOCK = Any()

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(LOCK) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d(TAG, "Database created")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d(TAG, "Database opened")
                    }
                })
                .build()
        }
    }
}