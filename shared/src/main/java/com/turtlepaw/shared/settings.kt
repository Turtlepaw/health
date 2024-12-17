package com.turtlepaw.shared

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalTime

enum class Settings(private val key: String, private val default: Any?) {
    GOAL("goal", 15),
    STEP_GOAL("step_goal", 5000),
    SUN_THRESHOLD("threshold", 5000),
    TIMEOUT("timeout", LocalTime.of(20, 0)),
    WAKEUP("wakeup", LocalTime.of(5, 0)),
    BATTERY_SAVER("battery_saver", true),
    GOAL_NOTIFICATIONS("goal_notify", false),
    STATUS("status", false),
    DEFAULT_DEVICE("default_device", null),
    INTRODUCTION_COMPLETE("introduction_complete", false),
    SAMPLING_RATE("sampling_rate", 5),
    NOISE_THRESHOLD("threshold", MAX_HEARING);

    fun getKey(): String {
        return key
    }

    fun getDefault(): String {
        return when (default) {
            is String -> {
                default
            }

            else -> {
                default.toString()
            }
        }
    }

    fun getDefaultOrNull(): String? {
        if (default == null) return null
        return when (default) {
            is String -> {
                default
            }

            else -> {
                default.toString()
            }
        }
    }

    fun getDefaultAsBoolean(): Boolean {
        return when (default) {
            is Boolean -> {
                default
            }

            else -> {
                false
            }
        }
    }

    fun getDefaultAsLocalTime(): LocalTime {
        return when (default) {
            is LocalTime -> {
                default
            }

            is String -> {
                LocalTime.parse(default)
            }

            else -> {
                LocalTime.of(10, 30)
            }
        }
    }

    fun getDefaultAsInt(): Int {
        return when (default) {
            is Int -> {
                default
            }

            is String -> {
                default.toInt()
            }

            else -> {
                0
            }
        }
    }

    fun getStringWith(sharedPreferences: SharedPreferences): String {
        return sharedPreferences.getString(key, (default ?: "").toString()).toString()
    }

    fun writeToSharedPreferences(sharedPreferences: SharedPreferences, value: Any?) {
        val editor = sharedPreferences.edit()

        when (default) {
            is Boolean -> editor.putBoolean(key, value as Boolean)
            is Int -> editor.putInt(key, value as Int)
            is String -> editor.putString(key, value as String)
            is Float -> editor.putFloat(key, value as Float)
            is Long -> editor.putLong(key, value as Long)
            is LocalTime -> editor.putString(key, (value as LocalTime).toString())
            else -> throw IllegalArgumentException("Unsupported type")
        }

        editor.apply() // Commit the changes asynchronously
    }

    fun readFromSharedPreferences(sharedPreferences: SharedPreferences): Any? {
        return when (default) {
            is Boolean -> sharedPreferences.getBoolean(key, default)
            is Int -> sharedPreferences.getInt(key, default)
            is String -> sharedPreferences.getString(key, default)
            is Float -> sharedPreferences.getFloat(key, default)
            is Long -> sharedPreferences.getLong(key, default)
            is LocalTime -> sharedPreferences.getString(key, default.toString())
                ?.let { LocalTime.parse(it) }

            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}

enum class SettingsBasics(private val key: String, private val mode: Int?) {
    HISTORY_STORAGE_BASE("bedtime_history", null),
    SHARED_PREFERENCES("HealthSettings", Context.MODE_PRIVATE);

    fun getKey(): String {
        return key
    }

    fun getMode(): Int {
        return mode ?: Context.MODE_PRIVATE
    }
}

fun Context.getDefaultSharedSettings(): SharedPreferences {
    return getSharedPreferences(
        SettingsBasics.SHARED_PREFERENCES.getKey(),
        SettingsBasics.SHARED_PREFERENCES.getMode()
    )
}