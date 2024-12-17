package com.turtlepaw.health.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.IntervalDataPoint
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.turtlepaw.shared.Settings
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.Day
import com.turtlepaw.shared.getDefaultSharedSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class HealthWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result = runBlocking {
        if (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("HealthWorker", "Permissions not granted, retrying")
            return@runBlocking Result.retry()
        }

        val deferred = CompletableDeferred<Unit>()

        val healthClient = HealthServices.getClient(context)
        val passiveMonitoringClient = healthClient.passiveMonitoringClient

        val passiveListenerCallback = object : PassiveListenerCallback {
            override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                super.onNewDataPointsReceived(dataPoints)
                val steps = stepsFromDataPoint(
                    dataPoints.getData(DataType.STEPS_DAILY)
                )
                Log.d("HealthWorker", "Steps are: $steps")

                // Update cat's status
                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getDatabase(context).dayDao().insertDay(
                        Day(
                            date = LocalDate.now(),
                            steps = steps.toInt(),
                            goal = context.getDefaultSharedSettings().getInt(
                                Settings.STEP_GOAL.getKey(),
                                Settings.STEP_GOAL.getDefaultAsInt()
                            )
                        )
                    )
                    // Signal completion
                    deferred.complete(Unit)
                }
            }
        }

        val passiveListenerConfig = PassiveListenerConfig.builder()
            .setDataTypes(
                setOf(
                    DataType.STEPS_DAILY
                )
            )
            .build()

        passiveMonitoringClient?.setPassiveListenerCallback(
            passiveListenerConfig!!,
            passiveListenerCallback
        )

        // Wait until data is received
        deferred.await()

        // Unregister measure callback
        withContext(Dispatchers.IO) {
            passiveMonitoringClient.clearPassiveListenerCallbackAsync()
        }

        return@runBlocking Result.success()
    }

    private fun stepsFromDataPoint(
        dataPoints: List<IntervalDataPoint<Long>>
    ): Long {
        var latest = 0
        var lastIndex = 0
        val bootInstant =
            Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime())

        if (dataPoints.isNotEmpty()) {
            dataPoints.forEachIndexed { index, intervalDataPoint ->
                val endTime = intervalDataPoint.getEndInstant(bootInstant)
                if (endTime.toEpochMilli() > latest) {
                    latest = endTime.toEpochMilli().toInt()
                    lastIndex = index
                }
            }

            return dataPoints[lastIndex].value
        } else return 0L
    }
}

fun Context.scheduleHealthWorker() {
    val manager = WorkManager.getInstance(this)

// Calculate delay until midnight
    val now = LocalDateTime.now()
    val midnight = now.with(LocalTime.MIDNIGHT).plusDays(1) // Midnight of next day
    val delay = Duration.between(now, midnight).toMillis()

    val workRequest = PeriodicWorkRequestBuilder<HealthWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS) // Set initial delay
        .build()

    manager.enqueueUniquePeriodicWork(
        "healthWorker",
        ExistingPeriodicWorkPolicy.UPDATE,
        workRequest
    )

    manager.enqueueUniqueWork(
        "healthWorkerOneTime",
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<HealthWorker>().build()
    )
}