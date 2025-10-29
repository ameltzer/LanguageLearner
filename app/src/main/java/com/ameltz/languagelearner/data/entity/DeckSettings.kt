package com.ameltz.languagelearner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity
class DeckSettings (
    @PrimaryKey val uuid: Uuid,
    var numShowDay: Int,
    var learningProgression: List<Int>
)
