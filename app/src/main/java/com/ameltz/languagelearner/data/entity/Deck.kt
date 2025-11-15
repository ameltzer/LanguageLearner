package com.ameltz.languagelearner.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import kotlin.uuid.Uuid

@Entity(indices = [Index(value = ["name"], unique = true)])
class Deck (
    @PrimaryKey val uuid: Uuid,
    val name: String,
    var deckSettingsId: Uuid
) {
    fun toHomePageDeckSummary(toDeckManagement: () -> Unit, todaysDeck: Uuid,
                              newCardsDue:Int = 0, reviewCardsDue:Int = 0, errorCardsDue:Int = 0): HomePageDeckModel {
        return HomePageDeckModel(name, newCardsDue, reviewCardsDue, errorCardsDue, toDeckManagement,
            todaysDeck)
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