package com.turtlepaw.health

import android.content.Context
import android.content.SharedPreferences
import com.turtlepaw.shared.Settings
import com.turtlepaw.shared.SettingsBasics
import java.time.LocalTime

class SharedPrefs(val context: Context) {
    private val sharedPrefs = getSharedPrefs()

    fun getSharedPrefs(): SharedPreferences {
        return context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
    }

    fun setLastSynced() {
        sharedPrefs.edit().putString(LAST_SYNC, LocalTime.now().toString()).apply()
    }

    fun getLastSynced(): LocalTime? {
        return try {
            LocalTime.parse(sharedPrefs.getString(LAST_SYNC, LocalTime.now().toString()))
        } catch (e: Exception) {
            null
        }
    }

    fun setSunlightGoal(goal: Int) {
        sharedPrefs.edit().putInt(SUNLIGHT_GOAL, goal).apply()
    }

    fun getSunlightGoal(): Int {
        return sharedPrefs.getInt(Settings.GOAL.getKey(), Settings.GOAL.getDefaultAsInt())
    }

    companion object {
        const val LAST_SYNC = "last_sync"
        const val SUNLIGHT_GOAL = "sunlight_goal"
    }
}