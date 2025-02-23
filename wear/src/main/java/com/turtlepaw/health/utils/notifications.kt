package com.turtlepaw.health.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class HealthNotifications {
    companion object {
        const val NOTIFICATION_CHANNEL =
            "FOREGROUND"
        private const val NOTIFICATION_CHANNEL_DISPLAY = "Foreground Services"

        const val EXERCISE_NOTIFICATION_CHANNEL =
            "EXERCISE_FOREGROUND"
        private const val EXERCISE_NOTIFICATION_CHANNEL_DISPLAY = "Exercises"
        const val SLEEP_NOTIFICATION_CHANNEL = "SLEEP_TRACKING"
        private const val SLEEP_NOTIFICATION_CHANNEL_DISPLAY = "Sleep Tracking"
    }

    /**
        Creates a unified notification channel for foreground notifications
     */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            NOTIFICATION_CHANNEL_DISPLAY,
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Channel for foreground service notifications, you may disable it."
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    /**
    Creates a unified notification channel for exercise foreground notifications
     */
    fun createExerciseChannel(context: Context) {
        val channel = NotificationChannel(
            EXERCISE_NOTIFICATION_CHANNEL,
            EXERCISE_NOTIFICATION_CHANNEL_DISPLAY,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for exercise foreground service notifications."
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun createSleepChannel(context: Context) {
        val channel = NotificationChannel(
            SLEEP_NOTIFICATION_CHANNEL,
            SLEEP_NOTIFICATION_CHANNEL_DISPLAY,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for sleep tracking notifications."
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}