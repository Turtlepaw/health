package com.turtlepaw.heart_connection

import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseType

data class Metric(
    val id: Int,
    val name: String,
    val icon: Int
)

val HeartRateMetric = Metric(
    id = 1,
    name = "Heart Rate",
    icon = R.drawable.heart
)

val DistanceMetric = Metric(
    id = 2,
    name = "Distance",
    icon = R.drawable.distance
)

val ElapsedTimeMetric = Metric(
    id = 3,
    name = "Elapsed Time",
    icon = R.drawable.timer
)

val CaloriesMetric = Metric(
    id = 4,
    name = "Calories",
    icon = R.drawable.calorie
)

val StepsMetric = Metric(
    id = 5,
    name = "Steps",
    icon = R.drawable.steps
)

val Metrics = listOf(
    ElapsedTimeMetric,
    HeartRateMetric,
    DistanceMetric,
    CaloriesMetric,
    StepsMetric
)

data class Exercise(
    val name: String,
    val defaultMetrics: List<Metric>,
    val icon: Int,
    val mapped: ExerciseType,
    val useGps: Boolean = false,
    val dataTypes: Set<DataType<*, *>> = emptySet()
)

val Workout = Exercise(
    name = "Workout",
    defaultMetrics = Metrics,
    icon = R.drawable.sports_martial_arts,
    mapped = ExerciseType.WORKOUT,
    useGps = false,
    dataTypes = setOf(
        DataType.HEART_RATE_BPM,
        DataType.CALORIES_TOTAL,
        DataType.STEPS_TOTAL,
    )
)

val Running = Exercise(
    name = "Running",
    defaultMetrics = listOf(
        StepsMetric,
        HeartRateMetric,
        DistanceMetric,
        ElapsedTimeMetric
    ),
    icon = R.drawable.run,
    mapped = ExerciseType.RUNNING,
    useGps = true
)

val Walking = Exercise(
    name = "Walking",
    defaultMetrics = listOf(
        StepsMetric,
        HeartRateMetric,
        DistanceMetric,
        ElapsedTimeMetric
    ),
    icon = R.drawable.walk,
    mapped = ExerciseType.WALKING,
    useGps = true
)

val Exercises = listOf(
    Workout,
    Running,
    Walking
)