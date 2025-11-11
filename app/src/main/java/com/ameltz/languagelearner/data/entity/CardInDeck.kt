package com.ameltz.languagelearner.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlin.uuid.Uuid

@Entity(
    indices=[Index(value = ["cardId", "deckId"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Card::class,
        parentColumns = ["uuid"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Deck::class,
        parentColumns = ["uuid"],
        childColumns = ["deckId"],
    )]
)
data class CardInDeck(
    @PrimaryKey val uuid: Uuid,
    var learnLevel: Int,
    var numberOfTimesShown: Int,
    val cardId: Uuid,
    val deckId: Uuid
) {
    companion object {
        fun createCardInDeck(cardId: Uuid, deckId: Uuid): CardInDeck {
            return CardInDeck(Uuid.random(), 0, 0, cardId, deckId)
        }
    }
}

data class CardInDeckAndDeckRelation(
    @Embedded val deck: Deck,
    @Relation(
        entity = CardInDeck::class,
        parentColumn="uuid",
        entityColumn="deckId"
    )
    val cardsInDeck: List<CardInDeckWithCard>
)
data class CardInDeckAndCardRelation(
    @Embedded val card: Card,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "cardId"
    )
    val instancesOfCard: List<CardInDeck>
)
data class CardInDeckWithCard(
    @Embedded val cardInDeck: CardInDeck,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "uuid"
    )
    val card: Card
)