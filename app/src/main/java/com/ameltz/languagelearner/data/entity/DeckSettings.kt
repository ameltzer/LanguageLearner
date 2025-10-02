package com.ameltz.languagelearner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
class DeckSettings(
    @PrimaryKey val uuid: UUID,
    var numShowDay: Int,
    var learningProgression: List<Int>
)
