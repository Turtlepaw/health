package com.turtlepaw.shared.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.turtlepaw.shared.adapters.LocalDateTimeTypeAdapter
import com.turtlepaw.shared.adapters.LocalDateTypeAdapter
import com.turtlepaw.shared.adapters.LocalTimeTypeAdapter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

inline fun <reified T> Gson.getTypeToken() = object : TypeToken<T>() {}.type

object Serializer {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter())
        .create()

    inline fun <reified T> serialize(data: T): String {
        return gson.toJson(data)
    }

    inline fun <reified T> deserialize(json: String): T {
        val type = object : TypeToken<T>() {}.type
        return gson.fromJson(json, type)
    }

    inline fun <reified T> deserializeList(json: String): List<T> {
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(json, type)
    }

    inline fun <reified T> fromJson(data: Any): T {
        return gson.fromJson(gson.toJson(data), T::class.java)
    }
}