package com.turtlepaw.health.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface SunlightDao {
    @Query("SELECT * FROM sunlight_day ORDER BY timestamp DESC")
    suspend fun getHistory(): List<SunlightDay>

    @Query("DELETE FROM sunlight_day WHERE timestamp = :day")
    suspend fun deleteDay(day: LocalDate)

    @Query("UPDATE sunlight_day SET value = :value WHERE timestamp = :day")
    suspend fun updateDay(day: LocalDate, value: Int)

    @Query("SELECT * FROM sunlight_day WHERE timestamp = :day")
    suspend fun getDay(day: LocalDate): SunlightDay?

    @Query("SELECT * FROM sunlight_day WHERE timestamp = :day LIMIT 1")
    fun getLiveDay(day: LocalDate): Flow<SunlightDay?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(sunlightDay: SunlightDay)
}

@Dao
interface SleepDao {
    @Insert
    suspend fun insertDay(day: SleepDay)

    @Query("UPDATE sleep_day SET wakeup = :wakeup WHERE bedtime = :bedtime")
    suspend fun updateWakeup(bedtime: LocalDateTime, wakeup: LocalDateTime)

    @Query("UPDATE sleep_day SET asleepAt = :asleepAt WHERE bedtime = :bedtime")
    suspend fun updateAsleepAt(bedtime: LocalDateTime, asleepAt: LocalDateTime)

    @Query("SELECT * FROM sleep_day WHERE date(bedtime) = date('now')")
    suspend fun getToday(): List<SleepDay>

    @Query("SELECT * FROM sleep_day WHERE substr(bedtime, 1, 10) = :day LIMIT 1")
    suspend fun getDay(day: String): SleepDay?

    @Query("SELECT * FROM sleep_day ORDER BY bedtime DESC")
    suspend fun getHistory(): List<SleepDay>

    @Query("DELETE FROM sleep_day")
    suspend fun deleteAll()
}

@Dao
interface ReflectionDao {
    @Insert
    suspend fun insertReflection(day: Reflection)

    // get latest
    @Query("SELECT * FROM reflection ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): Reflection?

    @Query("SELECT * FROM reflection ORDER BY date DESC")
    suspend fun getHistory(): List<Reflection>

    @Query("DELETE FROM reflection")
    suspend fun deleteAll()
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE name = :serviceName")
    suspend fun getService(serviceName: String): Service?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: Service)

    @Update
    suspend fun updateService(service: Service)

    @Query("UPDATE services SET isEnabled = :isEnabled WHERE name = :serviceName")
    suspend fun updateServiceStatus(serviceName: String, isEnabled: Boolean)

    @Query("SELECT * FROM services WHERE isEnabled = 1")
    suspend fun getEnabledServices(): List<Service>

    @Delete
    suspend fun deleteService(service: Service)
}
