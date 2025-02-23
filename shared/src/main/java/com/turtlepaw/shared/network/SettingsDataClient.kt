package com.turtlepaw.shared.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.turtlepaw.shared.SettingsBasics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

enum class ClientType {
    Mobile,
    Wear
}

/**
 * A client to sync shared preferences between the phone and the watch.
 *
 * This implementation provides helper methods to:
 * - Monitor local shared preference changes (which will trigger a sync).
 * - Update a particular preference and sync it.
 * - Receive a remote change and update local storage accordingly.
 *
 * Note: You'll need to add your networking logic in the designated TODO areas.
 */
class SettingsDataClient private constructor(
    private val context: Context,
    private val clientType: ClientType
) {
    private val networkClient by lazy { NetworkClient(context) }
    private val suppressListener = AtomicBoolean(false)

    companion object {
        private const val TAG = "SettingsDataClient"
        private var instance: SettingsDataClient? = null

        /**
         * Path for shared preferences received from [ClientType.Wear]
         */
        const val SHARED_PREFERENCES_PATH_WEAR = "/shared_preferences_wear"

        /**
         * Path for shared preferences received from [ClientType.Mobile]
         */
        const val SHARED_PREFERENCES_PATH_MOBILE = "/shared_preferences_mobile"
        private fun getSharedPreferencesPath(clientType: ClientType): String {
            return when (clientType) {
                ClientType.Mobile -> SHARED_PREFERENCES_PATH_MOBILE
                ClientType.Wear -> SHARED_PREFERENCES_PATH_WEAR
            }
        }

        /**
         * Returns a singleton instance of SettingsDataClient.
         */
        fun getInstance(context: Context, clientType: ClientType): SettingsDataClient {
            if (instance == null) {
                instance = SettingsDataClient(context.applicationContext, clientType)
            }
            return instance!!
        }
    }

    // Get shared preferences file; feel free to change the file name.
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
    }

    // Keep a strong reference to the listener so that it won't be garbage collected.
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            // Check if this change occurred as a result of a remote update.
            if (suppressListener.getAndSet(false)) {
                Log.d(TAG, "Preference change from remote update suppressed for key: $key")
                return@OnSharedPreferenceChangeListener
            }
            Log.d(TAG, "Local preference changed detected: key = $key")
            // When a setting changes locally, perform a sync for that particular preference.
            if (key != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    syncSetting(key)
                }
            }
        }

    init {
        // Register the listener for preference changes.
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    /**
     * Syncs all local settings to the remote device.
     */
    suspend fun syncAllSettings() {
        val allSettings: Map<String, *> = sharedPreferences.all
        Log.d(TAG, "Syncing all settings: $allSettings")
        networkClient.sendMap(allSettings, getSharedPreferencesPath(clientType))
    }

    /**
     * Syncs a single setting to the remote device.
     *
     * @param key the shared preference key to sync.
     */
    private suspend fun syncSetting(key: String) {
        val value = sharedPreferences.all[key]
        Log.d(TAG, "Syncing setting: $key = $value")
        networkClient.sendMap(mapOf(key to value), getSharedPreferencesPath(clientType))
    }

    /**
     * Updates a setting locally and triggers a sync with the remote device.
     *
     * @param key the setting key.
     * @param value the new value for the setting.
     * @throws IllegalArgumentException if value has an unsupported type.
     */
    fun updateSetting(key: String, value: Any) {
        sharedPreferences.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                else -> throw IllegalArgumentException("Unsupported preference type for key: $key")
            }
        }
        Log.d(TAG, "Updated setting locally: $key = $value")
        // The preference change listener will automatically trigger syncSetting.
    }

    /**
     * Handles a setting change received from the remote device.
     * This can be used when the watch changes a setting,
     * and the phone should update its local shared preferences without triggering a loop.
     *
     * @param key the setting key.
     * @param value the new value; if null, the setting is removed.
     */
    fun onRemoteSettingChanged(key: String, value: Any?) {
        Log.d(TAG, "Remote setting changed: $key = $value")
        // Set the flag so that the listener can ignore the update caused by this call.
        suppressListener.set(true)
        sharedPreferences.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                null -> remove(key)
                else -> throw IllegalArgumentException("Unsupported preference type for key: $key")
            }
        }
        Log.d(TAG, "Updated local setting from remote change: $key")
    }
}