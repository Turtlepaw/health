package com.turtlepaw.health.apps.sleep

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SleepTrackerState {
    private val _isTracking = MutableStateFlow(false)
    private val _isPaused = MutableStateFlow(false)

    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    fun updateTrackingState(isTracking: Boolean) {
        _isTracking.value = isTracking
    }

    fun updatePausedState(isPaused: Boolean) {
        _isPaused.value = isPaused
    }
}