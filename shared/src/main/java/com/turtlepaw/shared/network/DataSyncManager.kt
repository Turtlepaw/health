package com.turtlepaw.shared.network

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.SyncableDao
import com.turtlepaw.shared.database.SyncableEntity
import kotlinx.coroutines.tasks.await
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

typealias AnyDao = SyncableDao<out SyncableEntity, *>

class DataSyncManager(
    private val daos: List<AnyDao>,
    private val context: Context
) {
    val networkClient by lazy { NetworkClient(context) }

    companion object {
        // Static instance creation with all required dependencies
        fun create(context: Context, db: AppDatabase): DataSyncManager {
            return DataSyncManager(
                daos = listOf(
                    db.exerciseDao(),
                    db.sleepDao(),
                    db.sleepDataPointDao(),
                    db.reflectionDao(),
                    db.sunlightDao(),
                    db.serviceDao()
                ),
                context = context
            )
        }

        const val APP_INSTALLED_CAPABILITY_NAME = "installed"
        const val SYNC_DATA_PATH = "/sync_data"
    }

    suspend fun processSyncData(event: DataEvent) {
        if (event.type != DataEvent.TYPE_CHANGED) return
        if (event.dataItem.uri.path != SYNC_DATA_PATH) return

        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

        // Process all DAO data using DataSyncClient
        daos.forEach { dao ->
            dao::class.simpleName?.let { daoName ->
                dataMap.getString(daoName)?.let { json ->
                    DataSyncClient.receiveData(context, json)
                }
            }
        }
    }

    suspend fun requestSync(): Boolean {
        val messageClient = Wearable.getMessageClient(context)
        val capabilityInfo: CapabilityInfo = Wearable.getCapabilityClient(context)
            .getCapability(
                APP_INSTALLED_CAPABILITY_NAME,
                CapabilityClient.FILTER_REACHABLE
            ).await()

        for (node in capabilityInfo.nodes) {
            try {
                messageClient.sendMessage(
                    node.id,
                    SYNC_DATA_PATH,
                    ByteArray(0)
                ).await()
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

        return true
    }

    suspend fun performSync(): Boolean {
        val syncData = daos.mapNotNull { dao ->
            val unsynced = dao.getUnsynced()
            if (unsynced.isEmpty()) null
            else dao::class.simpleName!! to DataSyncClient.prepareData(unsynced)
        }.toMap()


        return try {
            networkClient.sendMap(syncData, SYNC_DATA_PATH)
            daos.forEach { dao ->
                try {
                    val unsyncedEntities = dao.getUnsynced()
                    val primaryKeys = unsyncedEntities.map { entity ->
                        // Use reflection to find the primary key
                        @Suppress("UNCHECKED_CAST")
                        val property = entity::class.memberProperties.first { property ->
                            property.isAccessible = true
                            property.annotations.any { annotation ->
                                annotation.annotationClass.simpleName == "PrimaryKey"
                            }
                        } as KProperty1<Any, *>

                        val value = property.get(entity as Any)
                            ?: throw IllegalStateException("Primary key must not be null")

                        // Don't convert to string, keep the original type
                        value
                    }

                    // Use unsafeCast because we know the types match at runtime
                    @Suppress("UNCHECKED_CAST")
                    (dao as SyncableDao<SyncableEntity, Any>).markSynced(primaryKeys)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}