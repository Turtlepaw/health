package com.turtlepaw.shared.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

//@Dao
//interface SleepDao {
//    @Insert
//    suspend fun insertDay(day: SleepDay)
//
//    @Query("UPDATE sleep_day SET wakeup = :wakeup WHERE bedtime = :bedtime")
//    suspend fun updateWakeup(bedtime: LocalDateTime, wakeup: LocalDateTime)
//
//    @Query("UPDATE sleep_day SET asleepAt = :asleepAt WHERE bedtime = :bedtime")
//    suspend fun updateAsleepAt(bedtime: LocalDateTime, asleepAt: LocalDateTime)
//
//    @Query("SELECT * FROM sleep_day WHERE date(bedtime) = date('now')")
//    suspend fun getToday(): List<SleepDay>
//
//    @Query("SELECT * FROM sleep_day WHERE substr(bedtime, 1, 10) = :day LIMIT 1")
//    suspend fun getDay(day: String): SleepDay?
//
//    @Query("SELECT * FROM sleep_day ORDER BY bedtime DESC")
//    suspend fun getHistory(): List<SleepDay>
//
//    @Query("DELETE FROM sleep_day")
//    suspend fun deleteAll()
//}

@Dao
interface CoachingProgramDao {
    @Insert
    suspend fun startProgram(program: CoachingProgram)

    @Query("SELECT * FROM coaching_program WHERE name = :programName")
    suspend fun getProgram(programName: String): CoachingProgram?

    @Update
    suspend fun updateProgram(program: CoachingProgram)

    @Delete
    suspend fun deleteProgram(program: CoachingProgram)
}