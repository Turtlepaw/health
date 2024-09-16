package com.turtlepaw.health.apps.sunlight.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.annotation.Keep
import com.turtlepaw.health.utils.Settings
import com.turtlepaw.health.utils.SettingsBasics

@Keep
class SunlightContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.turtlepaw.health.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/settings")
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(): Boolean {
        sharedPreferences = context!!.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        // Implement query logic if necessary
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        val result = Bundle()
        when (method) {
            "getGoal" -> {
                val value = sharedPreferences.getInt(
                    Settings.GOAL.getKey(),
                    Settings.GOAL.getDefaultAsInt()
                )
                result.putInt("result", value)
            }

            "getThreshold" -> {
                val value = sharedPreferences.getInt(
                    Settings.SUN_THRESHOLD.getKey(),
                    Settings.SUN_THRESHOLD.getDefaultAsInt()
                )
                result.putInt("result", value)
            }

            "getStatus" -> {
                val value = sharedPreferences.getBoolean(
                    Settings.STATUS.getKey(),
                    Settings.STATUS.getDefaultAsBoolean()
                )

                result.putBoolean("result", value)
            }
        }
        return result
    }
}
