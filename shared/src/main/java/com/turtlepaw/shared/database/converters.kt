package com.turtlepaw.shared.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.turtlepaw.heart_connection.Metric
import com.turtlepaw.heart_connection.Metrics
import com.turtlepaw.shared.database.reflect.ReflectionType
import com.turtlepaw.shared.database.services.ServiceType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromBedtimeSensor(value: BedtimeSensor?): String? {
        return value?.name
    }

    @TypeConverter
    fun toBedtimeSensor(value: String?): BedtimeSensor? {
        return value?.let { BedtimeSensor.valueOf(it) }
    }

    @TypeConverter
    fun fromServiceType(value: ServiceType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toServiceType(value: String?): ServiceType? {
        return value?.let { ServiceType.valueOf(it) }
    }

    @TypeConverter
    fun fromReflectionType(value: ReflectionType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toReflectionType(value: String?): ReflectionType? {
        return value?.let { ReflectionType.valueOf(it) }
    }

    @TypeConverter
    fun fromMap(value: Map<String, Boolean>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, Boolean> {
        val mapType = object : TypeToken<Map<String, Boolean>>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMetricList(metrics: List<Metric>?): String {
        return metrics?.joinToString(separator = ",") { it.id.toString() } ?: ""
    }

    @TypeConverter
    fun toMetricList(data: String): List<Metric> {
        if (data.isEmpty()) return emptyList()
        val ids = data.split(",").map { it.toInt() }
        return ids.mapNotNull { id -> Metrics.find { it.id == id } }
    }

    @TypeConverter
    fun fromIntList(intList: List<Int>): String {
        return intList.joinToString(separator = ",")
    }

    @TypeConverter
    fun toIntList(data: String): List<Int> {
        if (data.isEmpty()) return emptyList()
        return data.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromDuration(value: Duration?): Long? {
        return value?.toMillis()
    }

    @TypeConverter
    fun toDuration(value: Long?): Duration? {
        return value?.let { Duration.ofMillis(it) }
    }

    @TypeConverter
    fun fromHeartRateHistory(value: List<Pair<LocalTime, Int>>?): String? {
        Gson()
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toHeartRateHistory(value: String?): List<Pair<LocalTime, Int>>? {
        val type = object : TypeToken<List<Pair<LocalTime, Int>>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromPairDoubleList(value: List<Pair<Double, Double>>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPairDoubleList(value: String): List<Pair<Double, Double>> {
        val gson = Gson()
        val type = object : TypeToken<List<Pair<Double, Double>>>() {}.type
        return gson.fromJson(value, type)
    }
}
