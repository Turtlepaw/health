package com.turtlepaw.sleeptools.utils

import android.util.Log
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class TimeDifference(val hours: Long, val minutes: Long)
enum class AlarmType {
    SYSTEM_ALARM,
    USER_DEFINED
}

enum class SleepQuality(private val title: String, private val color: Int) {
    GOOD("Good", android.graphics.Color.parseColor("#71b219")),
    MEDIUM("Fair", android.graphics.Color.parseColor("#efa300")),
    POOR("Poor", android.graphics.Color.parseColor("#efa300"));

    fun getTitle(): String {
        return title
    }
}

class TimeManager {
    fun getDayFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("E d")
    }

    fun getTimeFormatter(withDayState: Boolean = true): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("h:mm${if(withDayState) " a" else ""}")
    }

    fun calculateSleepQuality(sleepTime: TimeDifference): SleepQuality {
        return if(sleepTime.hours >= 8) SleepQuality.GOOD
        else if(sleepTime.hours in 7..7) SleepQuality.MEDIUM
        else SleepQuality.POOR
    }

    fun calculateTimeDifference(targetTime: LocalTime, now: LocalTime = LocalTime.now()): TimeDifference {
        val currentDateTime = LocalTime.now()
        val duration = if (targetTime.isBefore(now)) {
            // If the target time is before the current time, it's on the next day
            Duration.between(currentDateTime, targetTime).plusDays(1)
        } else {
            Duration.between(currentDateTime, targetTime)
        }

        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes() + 1
        return if(minutes.toString() == "60")
            TimeDifference(hours + 1, 0)
        else TimeDifference(hours, minutes)
    }

    fun parseTime(time: String?, fallback: LocalTime? = null): LocalTime {
        return parseTimeOrNull(time) ?: (fallback ?: LocalTime.MIN)
    }

    fun parseTimeOrNull(time: String?): LocalTime? {
        if(time == null) return null
        return try {
            LocalTime.parse(time)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun parseDateTimeOrNull(time: String?): LocalDateTime? {
        if(time == null) return null
        return try {
            LocalDateTime.parse(time)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun parseDateTime(time: String?, fallback: LocalDateTime? = null): LocalDateTime {
        return try {
            LocalDateTime.parse(time)
        } catch (e: DateTimeParseException) {
            fallback ?: LocalDateTime.now()
        }
    }

    /**
     * @return Pair of wake time and type of wake time (`USER_DEFINED` or `SYSTEM_ALARM`)
     */
    fun getWakeTime(
        useAlarm: Boolean,
        nextAlarm: LocalTime?,
        wakeTime: String?,
        fallback: LocalTime?
    ): Pair<LocalTime, AlarmType> {
        val parsedTime = parseTime(wakeTime, fallback)

        return if (useAlarm) {
            Log.d("TimeManager", "Next alarm: $nextAlarm")
            val alarmType = if (nextAlarm == null)
                AlarmType.USER_DEFINED else AlarmType.SYSTEM_ALARM
            Log.d("TimeManager", "Alarm type: $alarmType")
            Pair(nextAlarm ?: parsedTime, alarmType)
        } else {
            Pair(
                parsedTime,
                AlarmType.USER_DEFINED
            )
        }
    }

    fun calculateAvgBedtime(bedtimes: Set<Pair<LocalDateTime, BedtimeSensor>?>): LocalTime? {
        if(bedtimes.isEmpty()) return null
        val lastBedtimes = bedtimes.take(7)
        val totalSeconds = lastBedtimes.filterNotNull().sumOf {
            it.first.toLocalTime().toSecondOfDay()
        }
        val averageSeconds = totalSeconds / bedtimes.size

        return LocalTime.ofSecondOfDay(averageSeconds.toLong())
    }
}