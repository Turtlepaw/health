package com.turtlepaw.shared.network

import android.content.Context
import android.util.Log
import com.google.gson.JsonElement
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.exercise.Exercise
import com.turtlepaw.shared.database.reflect.Reflection
import com.turtlepaw.shared.database.services.Service
import com.turtlepaw.shared.database.sleep.SleepDataPointEntity
import com.turtlepaw.shared.database.sleep.SleepSession
import com.turtlepaw.shared.database.sunlight.SunlightDay

object DataSyncClient {
    private const val TAG = "DataSyncClient"

    /**
     * Wraps data with its type information for serialization
     */
    fun prepareData(data: Any): String {
        val wrapper = DataWrapper(
            type = when (data) {
                is SunlightDay -> "SunlightDay"
                is Reflection -> "Reflection"
                is Exercise -> "Exercise"
                is SleepSession -> "SleepSession"
                is SleepDataPointEntity -> "SleepDataPoint"
                is List<*> -> when (data.firstOrNull()) {
                    is SunlightDay -> "SunlightDayList"
                    is Reflection -> "ReflectionList"
                    is Exercise -> "ExerciseList"
                    is SleepSession -> "SleepSessionList"
                    is SleepDataPointEntity -> "SleepDataPointList"
                    is Service -> "ServiceList"
                    else -> throw IllegalArgumentException("Unsupported list type")
                }

                else -> throw IllegalArgumentException("Unsupported type")
            },
            data = data
        )
        return Serializer.serialize(wrapper)
    }

    /**
     * Receives data and inserts it into the local database.
     */
    suspend fun receiveData(context: Context, json: String) {
        val db = AppDatabase.getDatabase(context)

        try {
            val wrapper = Serializer.deserialize<DataWrapperJson>(json)
            val jsonData = wrapper.data

            when (wrapper.type) {
                "SunlightDay" -> {
                    val data = Serializer.gson.fromJson(jsonData, SunlightDay::class.java)
                    db.sunlightDao().insertDay(data)
                }

                "SunlightDayList" -> {
                    val data = Serializer.gson.fromJson<List<SunlightDay>>(
                        jsonData,
                        Serializer.gson.getTypeToken<List<SunlightDay>>()
                    )
                    data.forEach {
                        db.sunlightDao().insertDay(it)
                    }
                }

                "Reflection" -> {
                    val data = Serializer.gson.fromJson(jsonData, Reflection::class.java)
                    db.reflectionDao().insertReflection(data)
                }

                "ReflectionList" -> {
                    val data = Serializer.gson.fromJson<List<Reflection>>(
                        jsonData,
                        Serializer.gson.getTypeToken<List<Reflection>>()
                    )
                    data.forEach {
                        db.reflectionDao().insertReflection(it)
                    }
                }

                "Exercise" -> {
                    val data = Serializer.gson.fromJson(jsonData, Exercise::class.java)
                    db.exerciseDao().insertExercise(data)
                }

                "ExerciseList" -> {
                    val data = Serializer.gson.fromJson<List<Exercise>>(
                        jsonData,
                        Serializer.gson.getTypeToken<List<Exercise>>()
                    )
                    data.forEach {
                        db.exerciseDao().insertExercise(it)
                    }
                }

                "SleepSession" -> {
                    val data = Serializer.gson.fromJson(jsonData, SleepSession::class.java)
                    db.sleepDao().insertSession(data)
                }

                "SleepSessionList" -> {
                    val data = Serializer.gson.fromJson<List<SleepSession>>(
                        jsonData,
                        Serializer.gson.getTypeToken<List<SleepSession>>()
                    )
                    data.forEach {
                        db.sleepDao().insertSession(it)
                    }
                }

                "SleepDataPoint" -> {
                    val data = Serializer.gson.fromJson(jsonData, SleepDataPointEntity::class.java)
                    db.sleepDataPointDao().insertDataPoints(listOf(data))
                }

                "SleepDataPointList" -> {
                    val data = Serializer.gson.fromJson<List<SleepDataPointEntity>>(
                        jsonData,
                        Serializer.gson.getTypeToken<List<SleepDataPointEntity>>()
                    )
                    db.sleepDataPointDao().insertDataPoints(data)
                }

                "ServiceList" -> {
                    val data = Serializer.gson.fromJson<List<Service>>(
                        jsonData,
                        Serializer.gson.getTypeToken<List<Service>>()
                    )
                    data.forEach {
                        db.serviceDao().insertService(it)
                    }
                }

                else -> throw IllegalArgumentException("Unknown type: ${wrapper.type}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing data", e)
            throw e // Rethrow to allow caller to handle the error
        }
    }
}

/**
 * Wrapper class for serialized data that includes type information
 */
data class DataWrapper(
    val type: String,
    val data: Any
)

/**
 * Wrapper class for JSON deserialization that keeps the data as a JsonElement
 */
data class DataWrapperJson(
    val type: String,
    val data: JsonElement
)