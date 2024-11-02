package com.turtlepaw.heart_connection

import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseType

data class Metric(
    val id: Int,
    val name: String,
    val icon: Int,
    val dataType: DataType<*, *>? = null
)

/**
 * Checks if a metric is available for a given exercise.
 *
 * Note that [ElapsedTimeMetric] will always be available
 */
fun Metric.isAvailableFor(exercise: Exercise): Boolean {
    return if (this.dataType == null) true
    else exercise.dataTypes.contains(dataType)
}

val HeartRateMetric = Metric(
    id = 1,
    name = "Heart Rate",
    icon = R.drawable.heart,
    dataType = DataType.HEART_RATE_BPM
)

val DistanceMetric = Metric(
    id = 2,
    name = "Distance",
    icon = R.drawable.distance,
    dataType = DataType.DISTANCE
)

val ElapsedTimeMetric = Metric(
    id = 3,
    name = "Elapsed Time",
    icon = R.drawable.timer
)

val CaloriesMetric = Metric(
    id = 4,
    name = "Calories",
    icon = R.drawable.calorie,
    dataType = DataType.CALORIES
)

val StepsMetric = Metric(
    id = 5,
    name = "Steps",
    icon = R.drawable.steps,
    dataType = DataType.STEPS
)

val SunlightMetric = Metric(
    id = 6,
    name = "Sunlight",
    icon = R.drawable.sunlight
)

val Metrics = listOf(
    ElapsedTimeMetric,
    HeartRateMetric,
    DistanceMetric,
    CaloriesMetric,
    StepsMetric,
    SunlightMetric
)

data class Exercise(
    val name: String,
    val defaultMetrics: List<Metric>,
    val icon: Int,
    val animatedIcon: Int? = null,
    val mapped: ExerciseType,
    val useGps: Boolean = false,
    val dataTypes: Set<DataType<*, *>>
)

val Workout = Exercise(
    name = "Workout",
    defaultMetrics = listOf(
        HeartRateMetric,
        CaloriesMetric,
        DistanceMetric,
        ElapsedTimeMetric
    ),
    icon = R.drawable.sports_martial_arts,
    mapped = ExerciseType.WORKOUT,
    useGps = false,
    dataTypes = setOf(
        DataType.HEART_RATE_BPM,
        DataType.CALORIES,
        DataType.DISTANCE
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
    animatedIcon = R.drawable.animated_walk,
    mapped = ExerciseType.RUNNING,
    useGps = true,
    dataTypes = setOf(
        DataType.HEART_RATE_BPM,
        DataType.CALORIES,
        DataType.STEPS,
        DataType.DISTANCE,
        DataType.LOCATION
    )
)

val Walking = Exercise(
    name = "Walking",
    defaultMetrics = listOf(
        StepsMetric,
        HeartRateMetric,
        DistanceMetric,
        ElapsedTimeMetric
    ),
    //animatedIcon = R.drawable.animated_walk,
    icon = R.drawable.walk,
    mapped = ExerciseType.WALKING,
    useGps = true,
    dataTypes = setOf(
        DataType.HEART_RATE_BPM,
        DataType.CALORIES,
        DataType.STEPS,
        DataType.DISTANCE,
        DataType.LOCATION
    )
)

val Exercises = listOf(
    Workout,
    Running,
    Walking
)

fun Exercise.getId(): Int {
    return Exercises.indexOf(this)
}