package com.turtlepaw.health.apps.exercise.manager

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "New media notification posted")

        // Filter for media notifications
        if (sbn.notification.category == Notification.CATEGORY_TRANSPORT) {
            val extras = sbn.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: "Unknown Title"
            val artist = extras.getString(Notification.EXTRA_TEXT) ?: "Unknown Artist"
            sbn.notification.getLargeIcon()

            // Post to a LiveData or state management
            Log.d("NotificationListener", "Title: $title, Artist: $artist")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "Media notification removed")
    }
}
