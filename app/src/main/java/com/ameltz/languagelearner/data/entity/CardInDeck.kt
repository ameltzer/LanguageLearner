package com.ameltz.languagelearner.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(
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
    @PrimaryKey val uuid: UUID,
    var learnLevel: Int,
    var numberOfTimesShown: Int,
    val cardId: UUID,
    val deckId: UUID
)

data class CardInDeckAndDeckRelation(
    @Embedded val deck: Deck,
    @Relation(
        parentColumn="uuid",
        entityColumn="deckId"
    )
    val cardsInDeck: List<CardInDeck>
)

data class CardInDeckAndCardRelation(
    @Embedded val card: Card,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "cardId"
    )
    val instancesOfCard: List<CardInDeck>
)