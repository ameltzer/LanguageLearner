package com.ameltz.languagelearner.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import java.util.UUID

@Entity
class Deck(
    @PrimaryKey val uuid: UUID,
    val name: String,
    var deckSettingsId: UUID
) {
    fun toHomePageDeckSummary(newCardsDue:Int = 0, reviewCardsDue:Int = 0, errorCardsDue:Int = 0): HomePageDeckModel {
        return HomePageDeckModel(name, newCardsDue, reviewCardsDue, errorCardsDue)
    }
}

data class DeckAndDeckSettingsRelation(
    @Embedded val card: DeckSettings,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "deckSettingsId"
    )
    val decksWithSettings: List<Deck>
)