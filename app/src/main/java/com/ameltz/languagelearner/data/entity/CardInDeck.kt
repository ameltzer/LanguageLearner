package com.ameltz.languagelearner.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import com.ameltz.languagelearner.ui.model.StudyCardOfTheDay
import com.ameltz.languagelearner.ui.model.StudyDeckOfTheDay
import java.time.Instant
import java.time.temporal.ChronoUnit
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
        onDelete = ForeignKey.CASCADE
    )]
)
data class CardInDeck(
    @PrimaryKey val uuid: Uuid,
    val daysToNextShow: Int,
    val cardId: Uuid,
    val deckId: Uuid
) {
    companion object {
        fun createCardInDeck(cardId: Uuid, deckId: Uuid): CardInDeck {
            return CardInDeck(Uuid.random(), 0,  cardId, deckId)
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
) {
    fun generateStudyMaterial(toDeckManagement: () -> Unit,
                              numCardsToStudy: Int,
                              repository: Repository): Pair<HomePageDeckModel, StudyDeckOfTheDay> {
        val storedToStudy = repository.getStudyDeck(deck.uuid, Instant.now().truncatedTo(ChronoUnit.DAYS))
        val resolvedToStudy: StudyDeckOfTheDay
        if (storedToStudy == null) {
            // Take cards sorted by priority, then shuffle for random study order
            val selectedCards = cardsInDeck
                .sortedBy { it.cardInDeck.daysToNextShow }
                .take(numCardsToStudy)
                .shuffled()

            resolvedToStudy = StudyDeckOfTheDay(
                Uuid.random(),
                deck.uuid,
                selectedCards.mapIndexed { index, card -> card.toInitialStudyCard(index) },
                false,
                Instant.now().truncatedTo(ChronoUnit.DAYS)
            )
            repository.upsertStudyDeck(resolvedToStudy.toStudyDeck(repository))
        } else {
            resolvedToStudy = storedToStudy.toStudyDeckOfTheDay()
        }

        var newCards = 0
        var reviewCards = 0
        var learnCards = 0

        resolvedToStudy.cards.forEach { cardsInDeck ->
            if (cardsInDeck.isNewCard) {
                newCards +=1
            } else if (cardsInDeck.nextShowMins <= repository.getHardTimeDelay()) {
                learnCards +=1
            } else {
                reviewCards +=1
            }
        }

        val homePageSummary = deck.toHomePageDeckSummary(
            toDeckManagement,
            resolvedToStudy.studyDeck,
            newCards,
            reviewCards,
            learnCards
        )
        return Pair(homePageSummary, resolvedToStudy)
    }

}
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
) {
    fun toInitialStudyCard(sortOrder: Int): StudyCardOfTheDay {
        return StudyCardOfTheDay(
            card.front,
            card.back,
            false,
            0,
            cardInDeck.deckId,
            Uuid.random(),
            cardInDeck.daysToNextShow == 0,
            null,
            sortOrder
        )
    }
}