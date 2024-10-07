package com.turtlepaw.health.apps.exercise.manager

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.SummaryScreenState
import com.turtlepaw.heart_connection.Exercise
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

open class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private var exerciseService: ExerciseService? = null
    private var isBound = false
    var isPaused = false
    var isEnded = false
    var isEnding = false
    var error: String? = null

    val _heartRate = MutableLiveData<Int>()
    val heartRate: LiveData<Int> get() = _heartRate

    val _heartRateHistory = MutableLiveData<List<Int>>()
    val heartRateHistory: LiveData<List<Int>> get() = _heartRateHistory

    val _calories = MutableLiveData<Double>()
    val calories: LiveData<Double> get() = _calories

    val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> get() = _distance

    val _duration = MutableLiveData<Long>()
    val duration: LiveData<Long> get() = _duration

    val _rawState = MutableLiveData<ExerciseUpdate>()
    val rawState: LiveData<ExerciseUpdate> get() = _rawState

    val _heartRateSource = MutableLiveData<HeartRateSource>()
    val heartRateSource: LiveData<HeartRateSource> get() = _heartRateSource

    val _availablities = MutableLiveData<Map<DataType<*, *>, Availability>>()
    val availablities: LiveData<Map<DataType<*, *>, Availability>> get() = _availablities

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ExerciseService.LocalBinder
            exerciseService = binder.getService()
            isBound = true

            exerciseService?.let { service ->
                service.getHeartRateLiveData().observeForever { _heartRate.postValue(it) }
                service.getCaloriesLiveData().observeForever { _calories.postValue(it) }
                service.getDistanceLiveData().observeForever { _distance.postValue(it) }
                service.getDurationLiveData().observeForever { _duration.postValue(it) }
                service.getRawDataLiveData().observeForever { _rawState.postValue(it) }
                service.getHeartRateSourceData().observeForever { _heartRateSource.postValue(it) }
                service.getAvailability().observeForever { _availablities.postValue(it) }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        Intent(getApplication(), ExerciseService::class.java).also { intent ->
            getApplication<Application>().bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isBound = false
        }
    }

    open fun attemptToReconnect() {
        exerciseService?.attemptToReconnect()
    }

    open suspend fun startExercise() {
        exerciseService?.startExerciseSession()
    }

    open suspend fun stopExercise() {
        exerciseService?.stopExerciseSession()
        isEnded = true
    }

    open suspend fun pauseExercise() {
        exerciseService?.pauseExerciseSession()
        isPaused = true
    }

    open suspend fun resumeExercise() {
        exerciseService?.resumeExerciseSession()
        isPaused = false
    }

    open fun reconnectHeartRateMonitor() {
        exerciseService?.attemptToReconnect()
    }

    open suspend fun warmExerciseSession(exercise: Exercise) {
        exerciseService?.warmExerciseSession(exercise)
    }

    open fun toSummary(): SummaryScreenState {
        return SummaryScreenState(
            averageHeartRate = heartRateHistory.value?.average() ?: 0.0,
            totalCalories = calories.value ?: 0.0,
            totalDistance = distance.value ?: 0.0,
            elapsedTime = duration.value?.toDuration(DurationUnit.SECONDS)?.toJavaDuration()
                ?: Duration.ZERO.toJavaDuration(),
            maxHeartRate = heartRateHistory.value?.maxOrNull() ?: 0.toInt(),
        )
    }
}

class FakeExerciseViewModel(application: Application) : ExerciseViewModel(application) {
    override fun onCleared() {}

    init {
        _heartRate.value = 120
        _heartRateHistory.value = listOf(110, 115, 120, 125, 130)
        _calories.value = 250.5
        _distance.value = 1200.7
        _duration.value = 3600
        _heartRateSource.value = HeartRateSource.HeartRateMonitor
    }

    override suspend fun startExercise() {}
    override suspend fun stopExercise() {}
    override suspend fun pauseExercise() {}
    override suspend fun resumeExercise() {}
    override suspend fun warmExerciseSession(exercise: Exercise) {}
    override fun attemptToReconnect() {}
    override fun reconnectHeartRateMonitor() {}
    override fun toSummary(): SummaryScreenState {
        return SummaryScreenState(
            averageHeartRate = 0.0,
            totalCalories = 0.0,
            totalDistance = 0.0,
            elapsedTime = Duration.ZERO.toJavaDuration(),
            maxHeartRate = 0
        )
    }
}
