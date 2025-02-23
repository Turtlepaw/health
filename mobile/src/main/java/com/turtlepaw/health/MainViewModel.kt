package com.turtlepaw.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.turtlepaw.shared.network.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val syncManager = SyncManager.getInstance(application)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState = _syncState.asStateFlow()

    init {
        viewModelScope.launch {
            syncManager.isSyncing.collect { isSyncing ->
                _syncState.value = if (isSyncing) SyncState.Syncing else SyncState.Idle
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _syncState.value = try {
                if (syncManager.sync()) {
                    SyncState.Success
                } else {
                    SyncState.Error("Sync failed")
                }
            } catch (e: Exception) {
                SyncState.Error(e.message ?: "Unknown error")
            }
        }
    }
}