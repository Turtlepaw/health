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
import androidx.health.services.client.data.DeltaDataType
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
import com.turtlepaw.heart_connection.Exercises
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalTime
import kotlin.math.abs

fun compareHeartRates(
    deviceList: List<Pair<LocalTime, Int>>,
    externalList: List<Pair<LocalTime, Int>>,
    timeThreshold: Long = 60 // Time difference threshold in seconds
): Double {

    var totalCount = 0
    var exactMatchCount = 0
    var veryCloseCount = 0
    var closeCount = 0
    var inaccurateCount = 0

    // Iterate through each time-heart rate pair in the device list
    for ((deviceTime, deviceRate) in deviceList) {
        // Find the closest time in the external list within the threshold
        val closestMatch = externalList.minByOrNull { (externalTime, _) ->
            abs(Duration.between(deviceTime, externalTime).seconds)
        }

        closestMatch?.let { (externalTime, externalRate) ->
            val timeDifference = abs(Duration.between(deviceTime, externalTime).seconds)
            if (timeDifference <= timeThreshold) {
                // Calculate the difference in heart rate values
                val rateDifference = abs(deviceRate - externalRate)

                // Categorize based on difference ranges
                when {
                    rateDifference == 0 -> exactMatchCount++
                    rateDifference <= 1 -> veryCloseCount++
                    rateDifference <= 5 -> closeCount++
                    else -> inaccurateCount++
                }

                totalCount++
            }
        }
    }

    // Calculate percentage of matches in each category
    val exactMatchPercentage =
        if (totalCount > 0) (exactMatchCount.toDouble() / totalCount) * 100 else 0.0
    val veryClosePercentage =
        if (totalCount > 0) (veryCloseCount.toDouble() / totalCount) * 100 else 0.0
    val closePercentage = if (totalCount > 0) (closeCount.toDouble() / totalCount) * 100 else 0.0
    val inaccuratePercentage =
        if (totalCount > 0) (inaccurateCount.toDouble() / totalCount) * 100 else 0.0

    // Calculate the overall accuracy as a weighted score
    return (exactMatchPercentage * 1.0) + (veryClosePercentage * 0.9) + (closePercentage * 0.7) + (inaccuratePercentage * 0.0)
}


fun canCompareHeartRates(
    deviceList: List<Pair<LocalTime, Int>>,
    externalList: List<Pair<LocalTime, Int>>,
    minOverlapMinutes: Long = 1
): Boolean {
    // Check if both lists contain data
    if (deviceList.isEmpty() || externalList.isEmpty()) {
        return false
    }

    // Find the minimum and maximum times for both lists
    val deviceStart = deviceList.minByOrNull { it.first }?.first
    val deviceEnd = deviceList.maxByOrNull { it.first }?.first
    val externalStart = externalList.minByOrNull { it.first }?.first
    val externalEnd = externalList.maxByOrNull { it.first }?.first

    // Ensure that both lists have valid time ranges
    if (deviceStart != null && deviceEnd != null && externalStart != null && externalEnd != null) {
        // Calculate the overlap between the device and external time ranges
        val overlapStart = maxOf(deviceStart, externalStart)
        val overlapEnd = minOf(deviceEnd, externalEnd)

        val overlapDuration = Duration.between(overlapStart, overlapEnd).toMinutes()

        // Check if the overlap is at least the minimum required
        return overlapDuration >= minOverlapMinutes
    }

    return false
}


open class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private var exerciseService: ExerciseService? = null
    private var isBound = false
    var error: String? = null

    val _isEnded = MutableLiveData<Boolean>()
    val isEnded: LiveData<Boolean> get() = _isEnded

    val _isPaused = MutableLiveData<Boolean>()
    val isPaused: LiveData<Boolean> get() = _isPaused

    val _isEnding = MutableLiveData<Boolean>(false)
    val isEnding: LiveData<Boolean> get() = _isEnding

    val _heartRate = MutableLiveData<Int>()
    val heartRate: LiveData<Int> get() = _heartRate

    val _heartRateHistory = MutableLiveData<List<Pair<LocalTime, Int>>>()
    val heartRateHistory: LiveData<List<Pair<LocalTime, Int>>> get() = _heartRateHistory

    val _deviceHrHistory = MutableLiveData<List<Pair<LocalTime, Int>>>()
    val deviceHrHistory: LiveData<List<Pair<LocalTime, Int>>> get() = _deviceHrHistory

    val _externalHrHistory = MutableLiveData<List<Pair<LocalTime, Int>>>()
    val externalHrHistory: LiveData<List<Pair<LocalTime, Int>>> get() = _externalHrHistory

    val _calories = MutableLiveData<Double>()
    val calories: LiveData<Double> get() = _calories

    val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> get() = _distance

    val _duration = MutableLiveData<Duration>()
    val duration: LiveData<Duration> get() = _duration

    val _rawState = MutableLiveData<ExerciseUpdate?>()
    val rawState: LiveData<ExerciseUpdate?> get() = _rawState

    val _heartRateSource = MutableLiveData<HeartRateSource>()
    val heartRateSource: LiveData<HeartRateSource> get() = _heartRateSource

    val _availabilities = MutableLiveData<Map<DataType<*, *>, Availability>>()
    val availabilities: LiveData<Map<DataType<*, *>, Availability>> get() = _availabilities

    val _steps = MutableLiveData<Long>()
    val steps: LiveData<Long> get() = _steps

    val _sunlightData = MutableLiveData<Int>()
    val sunlightData: LiveData<Int> get() = _sunlightData

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
                service.getDeviceHrHistoryLiveData()
                    .observeForever { _deviceHrHistory.postValue(it) }
                service.getExternalHistoryLiveData()
                    .observeForever { _externalHrHistory.postValue(it) }
                service.getSunlightLiveData().observeForever { _sunlightData.postValue(it) }
            }

            _isBound.postValue(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _isBound.postValue(false)
        }
    }

    private fun bindService(exercise: Exercise = Exercises.first()) {
        val exerciseId = Exercises.indexOf(exercise)
        Intent(getApplication(), ExerciseService::class.java).also { intent ->
            intent.putExtra("exerciseId", exerciseId)
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
        if (_isBound.value == true) {
            try {
                getApplication<Application>().unbindService(serviceConnection)
                _isBound.postValue(false)
            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Error unbinding service", e)
            }
        }
    }

    open fun attemptToReconnect() {
        exerciseService?.attemptToReconnect()
    }

    /**
     * Resets all live data values to their initial or empty states.
     * This effectively clears all stored workout data.
     */
    suspend fun flushData() {
        // Clear existing data
        _heartRateHistory.postValue(emptyList())
        _deviceHrHistory.postValue(emptyList())
        _externalHrHistory.postValue(emptyList())
        _calories.postValue(0.0)
        _distance.postValue(0.0)
        _steps.postValue(0)
        _duration.postValue(Duration.ZERO)
        _heartRateSource.postValue(HeartRateSource.Device)
        _availabilities.postValue(emptyMap())
        _rawState.postValue(null)
        _isEnded.postValue(false)
        _isEnding.postValue(false)
        _isPaused.postValue(false)
    }

    open suspend fun startExercise(exercise: Exercise) {
        flushData()
        getApplication<Application>().stopService(
            Intent(getApplication(), ExerciseService::class.java)
        )

        delay(100)

        bindService(exercise)
        _isBound.asFlow().first { it }
        exerciseService!!.startExerciseSession(exercise)
    }

    open suspend fun stopExercise() {
        _isEnding.postValue(true)
        exerciseService?.stopExerciseSession()
        getApplication<Application>().unbindService(serviceConnection)
        getApplication<Application>().stopService(
            Intent(getApplication(), ExerciseService::class.java)
        )
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
            Log.d("ExerciseViewModel", "Availability changed: $dataType, $data")
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

        var metrics: Set<DeltaDataType<*, *>> = setOf(DataType.HEART_RATE_BPM)
        if (exercise.useGps == true) metrics = metrics.plus(DataType.LOCATION)

        exerciseClient.prepareExercise(
            WarmUpConfig(
                exercise.mapped,
                metrics
            )
        )
        exerciseClient.setUpdateCallback(exerciseUpdateListener)
    }

    open fun toSummary(): SummaryScreenState {
        return SummaryScreenState(
            averageHeartRate = heartRateHistory.value?.map { it.second }?.average()?.toDouble()
                ?: 0.0,
            totalCalories = calories.value ?: 0.0,
            totalDistance = distance.value ?: 0.0,
            elapsedTime = (duration.value ?: Duration.ZERO),
            maxHeartRate = heartRateHistory.value?.map { it.second }?.maxOrNull() ?: 0.toInt(),
            steps = steps.value ?: 0,
            sunlight = sunlightData.value ?: 0,
            heartRate = heartRateHistory.value ?: emptyList(),
            heartRateSimilarity = if (canCompareHeartRates(
                    deviceHrHistory.value ?: emptyList(),
                    externalHrHistory.value ?: emptyList()
                ) == true
            ) {
                compareHeartRates(
                    deviceHrHistory.value ?: emptyList(),
                    externalHrHistory.value ?: emptyList()
                )
            } else {
                null
            }
        )
    }

    private fun calculateDuration(): Duration {
        if (rawState.value == null) return Duration.ZERO
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
        _heartRateHistory.value = listOf(110, 115, 120, 125, 130).map { LocalTime.now() to it }
        _calories.value = 250.5
        _distance.value = 1200.7
        _duration.value = Duration.ofMinutes(30)
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
            elapsedTime = Duration.ZERO,
            maxHeartRate = 0,
            sunlight = sunlightData.value ?: 0,
            heartRate = heartRateHistory.value ?: emptyList(),
            steps = steps.value
        )
    }
}
