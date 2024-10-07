package com.turtlepaw.health.apps.exercise.manager

import android.content.Context
import android.content.Intent

class ExerciseSessionManager(private val context: Context) {

    fun startSession() {
        val intent = Intent(context, ExerciseService::class.java)
        context.startService(intent)
    }

    fun pauseSession() {
        val intent = Intent(context, ExerciseService::class.java)
        context.startService(intent.apply { action = "ACTION_PAUSE" })
    }

    fun resumeSession() {
        val intent = Intent(context, ExerciseService::class.java)
        context.startService(intent.apply { action = "ACTION_RESUME" })
    }

    fun stopSession() {
        val intent = Intent(context, ExerciseService::class.java)
        context.stopService(intent)
    }
}
