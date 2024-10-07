package com.turtlepaw.health.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class HealthNotifications {
    companion object {
        const val NOTIFICATION_CHANNEL =
            "FOREGROUND"
        private const val NOTIFICATION_CHANNEL_DISPLAY = "Foreground Services"
    }

    /*
        Creates a unified notification channel for foreground notifications
     */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            NOTIFICATION_CHANNEL_DISPLAY,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for foreground service notifications, you may disable it."
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}