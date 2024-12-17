package com.turtlepaw.shared

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.Service
import com.turtlepaw.shared.database.ServiceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Prefixes(val prefix: String) {
    SharedPreference("shared_preferences"),
    ServiceDao("service_dao"),
}

class WearableSettingsSyncService() : WearableListenerService() {
    var onInitialized: (() -> Unit)? = null
    private val messageClient by lazy {
        Wearable.getMessageClient(
            this
        )
    }
    private val dataClient by lazy { Wearable.getDataClient(this) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var sharedPrefs: SharedPreferences
    lateinit var appDatabase: AppDatabase

    private val _states = mutableMapOf<String, MutableStateFlow<Any?>>()

    fun <T> getState(key: String): StateFlow<T?> {
        return _states.getOrElse(key) { MutableStateFlow<T?>(null) } as StateFlow<T?>
    }

    fun <T> updateState(key: String, value: T?) {
        val stateFlow =
            _states.getOrPut(key) { MutableStateFlow<Any?>(null) } as MutableStateFlow<T?>
        stateFlow.value = value
    }

    override fun onCreate() {
        Log.d("WearableSettingsSyncService", "onCreate - Entering")
        super.onCreate()
        Log.d("WearableSettingsSyncService", "onCreate - After super.onCreate()")
        sharedPrefs = getSharedPreferences( // Initialize in onCreate()
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        Log.d("WearableSettingsSyncService", "onCreate - After sharedPrefs initialization")
        appDatabase = AppDatabase.getDatabase(this)
        Log.d("WearableSettingsSyncService", "onCreate - After appDatabase initialization")
        onInitialized?.invoke()
        Log.d("WearableSettingsSyncService", "onCreate - Exiting")
    }

    fun initializeStates(): WearableSettingsSyncService {
        Settings.entries.forEach {
            updateState(it.getKey(), it.readFromSharedPreferences(sharedPrefs))
        }

        scope.launch {
            appDatabase.serviceDao().getAllServices().forEach {
                updateState(it.name, it.isEnabled)
            }
        }

        return this@WearableSettingsSyncService
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        appDatabase.close()
        Log.d("WearableSettingsSyncService", "onDestroy")
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            // DataItem changed
            if (event.type == DataEvent.TYPE_CHANGED) {
                event.dataItem.also { item ->
                    val dataItem = DataMapItem.fromDataItem(item).dataMap
                    if (item.uri.path?.startsWith(Prefixes.SharedPreference.prefix) == true) {
                        val key = dataItem.getString("key")
                        if (key == null) {
                            Log.e("WearableSettingsSyncService", "Key is null")
                            return@forEach
                        }

                        val value = dataItem.getString("value")
                        Log.d(
                            "WearableSettingsSyncService",
                            "Updating shared preference $key to $value"
                        )
                        Settings.valueOf(key).writeToSharedPreferences(sharedPrefs, value)
                        updateState(key, value)
                    } else if (item.uri.path?.startsWith(Prefixes.ServiceDao.prefix) == true) {
                        val key = dataItem.getString("key")
                        if (key == null) {
                            Log.e("WearableSettingsSyncService", "Key is null")
                            return@forEach
                        } else if (ServiceType.entries.toTypedArray()
                                .none { it.serviceName == key }
                        ) {
                            Log.e("WearableSettingsSyncService", "Key is not a service")
                            return@forEach
                        }

                        val value = dataItem.getBoolean("value")
                        scope.launch {
                            Log.d("WearableSettingsSyncService", "Updating service $key to $value")
                            appDatabase.serviceDao().insertService(
                                Service(
                                    key,
                                    value
                                )
                            )
                            updateState(key, value)
                        }
                    }
                }
            } else if (event.type == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    fun putSetting(setting: Settings, value: Any?) {
        if (value == null) {
            Log.e("WearableSettingsSyncService", "Value is null")
            return
        }

        Log.d(
            "WearableSettingsSyncService",
            "Updating shared preference ${setting.getKey()} to $value"
        )
        setting.writeToSharedPreferences(sharedPrefs, value)
        val putDataReq: PutDataRequest =
            PutDataMapRequest.create(
                Prefixes.SharedPreference.prefix
            ).run {
                dataMap.putString("key", setting.getKey())
                dataMap.putString("value", value.toString())
                asPutDataRequest()
            }.setUrgent()
        dataClient.putDataItem(putDataReq)
    }

    fun putService(service: ServiceType, value: Boolean) {
        Log.d("WearableSettingsSyncService", "Updating service ${service.serviceName} to $value")
        scope.launch {
            appDatabase.serviceDao().insertService(
                Service(
                    service.serviceName,
                    value
                )
            )
        }

        val putDataReq: PutDataRequest =
            PutDataMapRequest.create(
                Prefixes.ServiceDao.prefix
            ).run {
                dataMap.putString("key", service.serviceName)
                dataMap.putBoolean("value", value)
                asPutDataRequest()
            }.setUrgent()
        dataClient.putDataItem(putDataReq)
    }


}

class WearableSettingsSyncViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    val wearableSettingsSyncService = WearableSettingsSyncService()
    private val _isServiceReady = MutableStateFlow(false)
    val isServiceReady: StateFlow<Boolean> = _isServiceReady.asStateFlow()

    init {
        // Start the service
        Intent(getApplication(), WearableSettingsSyncService::class.java).also { intent ->
            getApplication<Application>().startService(intent)
        }

        // Track service initialization (e.g., using a callback or LiveData)
        viewModelScope.launch {
            // Wait for service to be ready
            // ... (e.g., using a callback or LiveData to signal readiness) ...

        }

        wearableSettingsSyncService.onInitialized = {
            _isServiceReady.value = true
            wearableSettingsSyncService.initializeStates()
        }
    }

    override fun onCleared() {
        super.onCleared()
        wearableSettingsSyncService.onDestroy()
    }
}