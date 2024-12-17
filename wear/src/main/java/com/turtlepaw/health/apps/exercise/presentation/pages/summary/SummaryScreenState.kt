package com.turtlepaw.health.apps.exercise.presentation.pages.summary

import java.time.Duration
import java.time.LocalTime

data class SummaryScreenState(
    val heartRate: List<Pair<LocalTime, Int>>,
    val averageHeartRate: Double?,
    val totalDistance: Double?,
    val totalCalories: Double?,
    val elapsedTime: Duration,
    val maxHeartRate: Int?,
    val steps: Long? = null,
    val heartRateSimilarity: Double? = null,
    val sunlight: Int,
    val locationData: List<Pair<Double, Double>>
)

const val averageHeartRateArg = "averageHeartRate"
const val totalDistanceArg = "totalDistance"
const val totalCaloriesArg = "totalCalories"
const val elapsedTimeArg = "elapsedTime"
const val maxHeartRateArg = "maxHeartRate"
const val stepsArg = "steps"
