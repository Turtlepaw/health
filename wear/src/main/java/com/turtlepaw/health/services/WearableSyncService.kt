package com.turtlepaw.health.services

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.network.ClientType
import com.turtlepaw.shared.network.DataSyncManager
import com.turtlepaw.shared.network.ServiceSyncClient
import com.turtlepaw.shared.network.SettingsDataClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WearableSyncService : WearableListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var dataSyncManager: DataSyncManager

    companion object {
        val TAG = WearableSyncService::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize your DAOs and DataSyncManager
        val database = AppDatabase.getDatabase(this)
        dataSyncManager = DataSyncManager.create(this, database)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: $dataEvents")

        // Create a local list copy of the data events to avoid accessing the buffer after it's released.
        val eventsList = mutableListOf<DataEvent>()
        try {
            for (event in dataEvents) {
                eventsList.add(event.freeze()) // freeze to obtain an immutable copy of the event
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying events", e)
        } finally {
            dataEvents.release()  // Now safe to release the buffer
        }

        // Process the copied list of events asynchronously.
        serviceScope.launch {
            eventsList.forEach { event ->
                try {
                    if (event.type != DataEvent.TYPE_CHANGED) return@forEach
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    if (event.dataItem.uri.path == SettingsDataClient.SHARED_PREFERENCES_PATH_MOBILE) {
                        dataMap?.keySet()?.forEach { key ->
                            if (key == "timestamp") return@forEach
                            dataMap.getString(key)?.let { json ->
                                Log.d(TAG, "Received shared settings data: $json")
                                SettingsDataClient.getInstance(applicationContext, ClientType.Wear)
                                    .onRemoteSettingChanged(key, json)
                            }
                        }
                    } else if (event.dataItem.uri.path == ServiceSyncClient.WEAR_SYNC_DATA_PATH) {
                        ServiceSyncClient.create(
                            applicationContext,
                            AppDatabase.getDatabase(applicationContext)
                        ).onDataReceived(
                            dataMap
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing event", e)
                }
            }
        }
    }

    override fun onMessageReceived(message: MessageEvent) {
        if (message.path == DataSyncManager.SYNC_DATA_PATH) {
            serviceScope.launch {
                dataSyncManager.performSync()
            }
        }
    }
}