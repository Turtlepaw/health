package com.turtlepaw.health

import android.content.Context
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.network.DataSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

class SyncManager private constructor(context: Context) {
    private val contextRef = WeakReference(context.applicationContext)
    private val database = AppDatabase.getDatabase(context)

    private val dataSyncManager by lazy {
        contextRef.get()?.let { appContext ->
            DataSyncManager(
                daos = listOf(
                    database.reflectionDao(),
                    database.exerciseDao(),
                    database.sleepDao()
                ),
                context = appContext
            )
        }
    }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    suspend fun sync(): Boolean {
        if (dataSyncManager == null) return false

        _isSyncing.value = true
        return try {
            dataSyncManager!!.requestSync()
        } catch (e: Exception) {
            _isSyncing.value = false
            throw e
        }
        // Note: We don't set isSyncing to false here anymore
        // It will be set by DataSyncListenerService when data is received
    }

    fun completeSyncing() {
        _isSyncing.value = false
    }

    companion object {
        @Volatile
        private var INSTANCE: SyncManager? = null

        fun getInstance(context: Context): SyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SyncManager(context).also { INSTANCE = it }
            }
        }
    }
}