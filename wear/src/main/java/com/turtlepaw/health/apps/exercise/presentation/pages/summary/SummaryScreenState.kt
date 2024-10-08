package com.turtlepaw.health.apps.exercise.presentation.pages.summary

import java.time.Duration

data class SummaryScreenState(
    val averageHeartRate: Double?,
    val totalDistance: Double?,
    val totalCalories: Double?,
    val elapsedTime: Duration,
    val maxHeartRate: Int?,
    val steps: Long? = null
)

const val averageHeartRateArg = "averageHeartRate"
const val totalDistanceArg = "totalDistance"
const val totalCaloriesArg = "totalCalories"
const val elapsedTimeArg = "elapsedTime"
const val maxHeartRateArg = "maxHeartRate"
const val stepsArg = "steps"
