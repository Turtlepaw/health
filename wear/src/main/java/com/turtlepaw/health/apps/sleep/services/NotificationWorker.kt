package com.turtlepaw.health.apps.sleep.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.turtlepaw.health.R
import com.turtlepaw.shared.theming.SleepColors
import kotlinx.coroutines.runBlocking

class NotificationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    companion object {
        const val CHANNEL_NAME = "Bedtime Notifications"
        const val CHANNEL_ID = "bedtime"
        const val NOTIFICATION_ID = 1
        const val WORKER_TAG = "bedtime_worker"
    }

    private fun createNotificationChannel(
        notificationManager: NotificationManager
    ){
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        )
    }

    override fun doWork(): Result {
        runBlocking {
            val notificationManager = appContext.getSystemService(NotificationManager::class.java)

            // Create notification channel
            createNotificationChannel(notificationManager)

            // Build the notification
            val builder = Notification.Builder(appContext, CHANNEL_ID)
                .setContentTitle("Bedtime")
                .setContentText("Time to wind down and prepare for a restful night.")
                .setColor(SleepColors.primary.toArgb())
                .setSmallIcon(R.drawable.sleep_white)
                .build()

            // Send the notification
            notificationManager.notify(
                NOTIFICATION_ID,
                builder
            )
        }
        return Result.success()
    }
}