package com.turtlepaw.health.services

import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class ResetWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result = runBlocking {
        val intent = Intent("${context.packageName}.SUNLIGHT_CHANGED").apply {
            putExtra("value", 0)
        }
        context.sendBroadcast(intent)
        return@runBlocking Result.success()
    }
}

fun Context.scheduleResetWorker() {
    val manager = WorkManager.getInstance(this)
    val workRequest = PeriodicWorkRequestBuilder<ResetWorker>(1, TimeUnit.DAYS)
        .build()
    manager.enqueueUniquePeriodicWork(
        "resetWorker",
        ExistingPeriodicWorkPolicy.UPDATE,
        workRequest
    )
}
