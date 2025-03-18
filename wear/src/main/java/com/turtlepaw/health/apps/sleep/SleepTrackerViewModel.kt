package com.turtlepaw.health.apps.sleep

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SleepTrackerViewModel(application: Application) : AndroidViewModel(application) {
    val _isTracking = MutableLiveData<Boolean>(false)
    val _isPaused = MutableLiveData<Boolean>(false)
    val _isSleeping = MutableLiveData<Boolean>(false)
    val _hints = MutableLiveData<Map<SleepTrackerHints, Any>>(emptyMap())


    val isTracking: LiveData<Boolean> get() = _isTracking
    val isPaused: LiveData<Boolean> get() = _isPaused
    val isSleeping: LiveData<Boolean> get() = _isSleeping
    val hints: LiveData<Map<SleepTrackerHints, Any>> get() = _hints

    private val _isBound = MutableLiveData<Boolean>(false)
    private var isBound = false
    private var sleepService: SleepTrackerService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SleepTrackerService.LocalBinder
            sleepService = binder.getService()
            isBound = true

            sleepService?.let { service ->
                // Update with current values immediately
                _isTracking.postValue(service.getTrackingState().value)
                _isPaused.postValue(service.getPausedState().value)
                _isSleeping.postValue(service.getSleepState().value)
                _hints.postValue(service.getActiveHints().value)

                // Then observe for future changes
                service.getTrackingState().observeForever { _isTracking.postValue(it) }
                service.getPausedState().observeForever { _isPaused.postValue(it) }
                service.getSleepState().observeForever { _isSleeping.postValue(it) }
                service.getActiveHints().observeForever { _hints.postValue(it) }
            }

            _isBound.postValue(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            sleepService = null
            isBound = false
            _isBound.postValue(false)
        }
    }

    init {
        bindService()
    }

    fun bindService() {
        Intent(getApplication(), SleepTrackerService::class.java).also { intent ->
            getApplication<Application>().bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (_isBound.value == true) {
            try {
                // Remove observers to prevent memory leaks
                sleepService?.let { service ->
                    service.getTrackingState().removeObserver { _isTracking.postValue(it) }
                    service.getPausedState().removeObserver { _isPaused.postValue(it) }
                    service.getSleepState().removeObserver { _isSleeping.postValue(it) }
                    service.getActiveHints().removeObserver { _hints.postValue(it) }
                }

                getApplication<Application>().unbindService(serviceConnection)
                _isBound.postValue(false)
            } catch (e: Exception) {
                Log.e(SleepTrackerViewModel::class.simpleName, "Error unbinding service", e)
            }
        }
    }

    fun startTracking(context: Context) {
        val intent = Intent(context, SleepTrackerService::class.java).apply {
            action = "START"
        }
        context.startForegroundService(intent)
    }

    fun stopTracking(context: Context) {
        val intent = Intent(context, SleepTrackerService::class.java).apply {
            action = "STOP"
        }
        context.startService(intent)
    }

    fun pauseTracking(context: Context) {
        val intent = Intent(context, SleepTrackerService::class.java).apply {
            action = "PAUSE"
        }
        context.startService(intent)
    }

    fun resumeTracking(context: Context) {
        val intent = Intent(context, SleepTrackerService::class.java).apply {
            action = "RESUME"
        }
        context.startService(intent)
    }
}