package com.ameltz.languagelearner.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity
class Deck(
    @PrimaryKey val uuid: UUID,
    val name: String,
    var deckSettingsId: UUID
)

data class DeckAndDeckSettingsRelation(
    @Embedded val card: DeckSettings,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "deckSettingsId"
    )
    val decksWithSettings: List<Deck>
)