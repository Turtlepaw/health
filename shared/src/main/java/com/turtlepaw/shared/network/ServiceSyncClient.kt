package com.turtlepaw.shared.network

import android.content.Context
import com.google.android.gms.wearable.DataMap
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.services.Service
import com.turtlepaw.shared.database.services.ServiceType

class ServiceSyncClient(
    private val context: Context,
    private val db: AppDatabase
) {
    val networkClient by lazy { NetworkClient(context) }

    companion object {
        // Static instance creation with all required dependencies
        fun create(context: Context, db: AppDatabase): ServiceSyncClient {
            return ServiceSyncClient(
                context = context,
                db = db
            )
        }

        const val APP_INSTALLED_CAPABILITY_NAME = "installed"
        const val WEAR_SYNC_DATA_PATH = "/services"

        suspend fun editService(db: AppDatabase, name: ServiceType, isEnabled: Boolean) {
            val service = db.serviceDao().getService(name)
            if (service == null) {
                db.serviceDao().insertService(
                    Service(
                        name = name,
                        isEnabled = isEnabled,
                        synced = false
                    )
                )
            } else {
                db.serviceDao().updateService(
                    serviceName = name,
                    isEnabled = isEnabled,
                    synced = false
                )
            }
        }
    }

    suspend fun onDataReceived(data: DataMap) {
        for (key in data.keySet()) {
            if (key == "timestamp") continue
            val isEnabled = try {
                data.getBoolean(key)
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }

            editService(db, ServiceType.valueOf(key), isEnabled)
        }
    }

    suspend fun setService(name: ServiceType, isEnabled: Boolean) {
        editService(db, name, isEnabled)
        performSync()
    }

    suspend fun performSync(): Boolean {
        val syncData = db.serviceDao().getAllServices().associate {
            it.name.toString() to it.isEnabled
        }

        return try {
            networkClient.sendMap(syncData, WEAR_SYNC_DATA_PATH)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}