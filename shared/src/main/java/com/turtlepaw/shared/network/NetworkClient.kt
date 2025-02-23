package com.turtlepaw.shared.network

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class NetworkClient(
    private val context: Context
) {
    suspend fun sendMap(data: Map<String, *>, path: String, isUrgent: Boolean = true) {
        val dataClient = Wearable.getDataClient(context)
        try {
            val request = PutDataMapRequest.create(path).apply {
                data.forEach { (key, value) ->
                    when (value) {
                        is String -> dataMap.putString(key, value)
                        is Int -> dataMap.putInt(key, value)
                        is Boolean -> dataMap.putBoolean(key, value)
                        is Long -> dataMap.putLong(key, value)
                        is Float -> dataMap.putFloat(key, value)
                        else -> throw IllegalArgumentException("Unsupported preference type for key: $key")
                    }
                }
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }
                .asPutDataRequest()
                .apply {
                    if (isUrgent) setUrgent()
                }

            val result = dataClient.putDataItem(request).await()

            Log.d(this::class.simpleName, "DataItem saved: $result")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(this::class.simpleName, "Saving DataItem failed: $exception")
        }
    }
}