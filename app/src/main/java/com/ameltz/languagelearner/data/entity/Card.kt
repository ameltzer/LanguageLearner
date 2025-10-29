package com.ameltz.languagelearner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity
data class Card(
    @PrimaryKey val uuid: Uuid,
    val front: String,
    val back: String
) {
    fun display(): String {
        return "$front - $back"
    }
}