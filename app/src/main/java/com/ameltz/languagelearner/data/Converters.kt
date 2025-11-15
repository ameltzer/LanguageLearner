package com.ameltz.languagelearner.data

import androidx.room.TypeConverter
import com.ameltz.languagelearner.data.entity.StudyCard
import com.ameltz.languagelearner.data.entity.StudyCardWithCard
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    @TypeConverter
    fun fromStudyCards(value: List<StudyCardWithCard>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStudyCards(value: String): List<StudyCardWithCard> {
        return Json.decodeFromString<List<StudyCardWithCard>>(value)
    }
}