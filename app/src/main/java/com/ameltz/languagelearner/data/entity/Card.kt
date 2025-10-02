package com.ameltz.languagelearner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Card(
    @PrimaryKey val uuid: UUID,
    val front: String,
    val back: String
)