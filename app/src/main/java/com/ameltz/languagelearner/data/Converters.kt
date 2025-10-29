package com.ameltz.languagelearner.data

import androidx.room.TypeConverter
import kotlin.uuid.Uuid

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int>? {
        return data?.split(",")?.map { it.toInt() }
    }

    @TypeConverter
    fun fromUuid(uuid: Uuid): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUuid(string: String): Uuid {
        return Uuid.parse(string)
    }
}