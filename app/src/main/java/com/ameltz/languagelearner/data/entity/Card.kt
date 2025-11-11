package com.ameltz.languagelearner.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(indices=[Index(value = ["front", "back"], unique = true)])
data class Card(
    @PrimaryKey val uuid: Uuid,
    val front: String,
    val back: String
) {
    fun display(): String {
        return "$front - $back"
    }

    companion object {
        fun createCard(front: String, back: String): Card {
            return Card(Uuid.random(), front, back)
        }
    }

}