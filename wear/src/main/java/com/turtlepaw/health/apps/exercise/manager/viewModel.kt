package com.turtlepaw.health.apps.exercise.manager

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseTrackedStatus.Companion.OWNED_EXERCISE_IN_PROGRESS
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.getCurrentExerciseInfo
import androidx.health.services.client.prepareExercise
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.SummaryScreenState
import com.turtlepaw.heart_connection.Exercise
import kotlinx.coroutines.flow.first
import kotlin.time.Duration
import kotlin.time.toJavaDuration

open class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private var exerciseService: ExerciseService? = null
    private var isBound = false
    var error: String? = null

    val _isEnded = MutableLiveData<Boolean>()
    val isEnded: LiveData<Boolean> get() = _isEnded

    val _isPaused = MutableLiveData<Boolean>()
    val isPaused: LiveData<Boolean> get() = _isPaused

    val _isEnding = MutableLiveData<Boolean>()
    val isEnding: LiveData<Boolean> get() = _isEnding

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

    val _availabilities = MutableLiveData<Map<DataType<*, *>, Availability>>()
    val availabilities: LiveData<Map<DataType<*, *>, Availability>> get() = _availabilities

    val _steps = MutableLiveData<Long>()
    val steps: LiveData<Long> get() = _steps

    private val _isBound = MutableLiveData<Boolean>(false)

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
                service.getStepsLiveData().observeForever { _steps.postValue(it) }
                service.getHeartRateHistoryLiveData()
                    .observeForever { _heartRateHistory.postValue(it) }
            }

            _isBound.postValue(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    private fun bindService() {
        Intent(getApplication(), ExerciseService::class.java).also { intent ->
            getApplication<Application>().startForegroundService(intent) // Start as foreground service
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

    open suspend fun startExercise(exercise: Exercise) {
        bindService()
        _isBound.asFlow().first { it }
        exerciseService!!.startExerciseSession(exercise)
    }

    open suspend fun stopExercise() {
        _isEnding.postValue(true)
        exerciseService?.stopExerciseSession()
        _isEnded.postValue(true)
    }

    open suspend fun pauseExercise() {
        exerciseService?.pauseExerciseSession()
        _isPaused.postValue(true)
    }

    open suspend fun resumeExercise() {
        exerciseService?.resumeExerciseSession()
        _isPaused.postValue(false)
    }

    open fun reconnectHeartRateMonitor() {
        exerciseService?.attemptToReconnect()
    }

    private val exerciseUpdateListener = object : ExerciseUpdateCallback {
        override fun onAvailabilityChanged(
            dataType: DataType<*, *>,
            data: Availability
        ) {
            _availabilities.postValue(
                (availabilities.value ?: emptyMap()).toMutableMap().plus(Pair(dataType, data))
            )
        }

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {}

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}

        override fun onRegistered() {}

        override fun onRegistrationFailed(throwable: Throwable) {}
    }

    open suspend fun warmExerciseSession(exercise: Exercise, context: Context) {
        val healthServicesClient = HealthServices.getClient(context)
        val exerciseClient = healthServicesClient.exerciseClient
        exerciseClient.setUpdateCallback(exerciseUpdateListener)

        val metrics = setOf(DataType.HEART_RATE_BPM)
        if (exercise.useGps) metrics.plus(DataType.LOCATION)

        exerciseClient.prepareExercise(
            WarmUpConfig(
                exercise.mapped,
                metrics
            )
        )
    }

    open fun toSummary(): SummaryScreenState {
        Log.d("toSummary", "${calculateDuration().seconds}")
        return SummaryScreenState(
            averageHeartRate = heartRateHistory.value?.average() ?: 0.0,
            totalCalories = calories.value ?: 0.0,
            totalDistance = distance.value ?: 0.0,
            elapsedTime = calculateDuration(),
            maxHeartRate = heartRateHistory.value?.maxOrNull() ?: 0.toInt(),
            steps = steps.value ?: 0
        )
    }

    private fun calculateDuration(): java.time.Duration {
        if (rawState.value == null) return Duration.ZERO.toJavaDuration()
        val state = rawState.value?.exerciseStateInfo?.state
        val checkpoint = rawState.value?.activeDurationCheckpoint
        val delta = if (state == ExerciseState.ACTIVE) {
            System.currentTimeMillis() - checkpoint?.time!!.toEpochMilli()
        } else {
            0L
        }
        return checkpoint!!.activeDuration.plusMillis(delta)
    }

    suspend fun isExerciseInProgress(context: Context): Pair<Boolean, Exercise?> {
        val healthServicesClient = HealthServices.getClient(context)
        val exerciseClient = healthServicesClient.exerciseClient

        return try {
            val exerciseInfo = exerciseClient.getCurrentExerciseInfo()
            when (exerciseInfo.exerciseTrackedStatus) {
                OWNED_EXERCISE_IN_PROGRESS -> {
                    bindService()
                    _isBound.asFlow().first { it }
                    true to exerciseService?.getCurrentExercise()
                }

                else -> false to null
            }
        } catch (e: Exception) {
            return false to null
        }
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

    override suspend fun startExercise(exercise: Exercise) {}
    override suspend fun stopExercise() {}
    override suspend fun pauseExercise() {}
    override suspend fun resumeExercise() {}
    override suspend fun warmExerciseSession(exercise: Exercise, context: Context) {}
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
