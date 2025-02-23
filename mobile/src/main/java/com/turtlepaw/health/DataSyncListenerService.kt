package com.turtlepaw.health

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.turtlepaw.shared.network.ClientType
import com.turtlepaw.shared.network.DataSyncClient
import com.turtlepaw.shared.network.DataSyncManager
import com.turtlepaw.shared.network.SettingsDataClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DataSyncListenerService : WearableListenerService() {
    companion object {
        val TAG = DataSyncListenerService::class.java.simpleName
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: $dataEvents")

        try {
            // Create a list to store the processed events
            val events = dataEvents.map { event ->
                // For each event, create an object with the necessary data
                ProcessedEvent(
                    type = event.type,
                    path = event.dataItem.uri.path,
                    dataMap = if (event.type == DataEvent.TYPE_CHANGED) {
                        DataMapItem.fromDataItem(event.dataItem).dataMap
                    } else null
                )
            }

            // Now process the events asynchronously
            scope.launch {
                events.forEach { processedEvent ->
                    try {
                        if (processedEvent.type != DataEvent.TYPE_CHANGED) return@forEach
                        if (processedEvent.path == DataSyncManager.SYNC_DATA_PATH) {
                            processedEvent.dataMap?.keySet()?.forEach { key ->
                                if (key == "timestamp") return@forEach
                                processedEvent.dataMap.getString(key)?.let { json ->
                                    Log.d(TAG, "Received sync data: $json")
                                    DataSyncClient.receiveData(applicationContext, json)
                                }
                            }
                        } else if (processedEvent.path == SettingsDataClient.SHARED_PREFERENCES_PATH_WEAR) {
                            processedEvent.dataMap?.keySet()?.forEach { key ->
                                if (key == "timestamp") return@forEach
                                processedEvent.dataMap.get<Any>(key)?.let { json ->
                                    Log.d(TAG, "Received shared settings data: $json")
                                    SettingsDataClient.getInstance(
                                        applicationContext,
                                        ClientType.Mobile
                                    ).onRemoteSettingChanged(key, json)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // Complete syncing after all processing is done
                SyncManager.getInstance(applicationContext).completeSyncing()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing events", e)
            SyncManager.getInstance(applicationContext).completeSyncing()
        } finally {
            dataEvents.release()  // Make sure to release the buffer
        }
    }
}

private data class ProcessedEvent(
    val type: Int,
    val path: String?,
    val dataMap: DataMap?
)