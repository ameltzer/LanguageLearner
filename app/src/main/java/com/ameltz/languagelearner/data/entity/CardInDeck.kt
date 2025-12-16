package com.ameltz.languagelearner.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ameltz.languagelearner.data.DeckDate
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import com.ameltz.languagelearner.ui.model.StudyCardOfTheDay
import com.ameltz.languagelearner.ui.model.StudyDeckOfTheDay
import java.time.Instant
import kotlin.uuid.Uuid

@Entity(
    indices=[Index(value = ["cardId", "deckId"], unique = true), Index(value=["deckId"])],
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
    val priority: Int,
    val easyCount: Int,
    val mediumCount: Int,
    val hardCount: Int,
    val cardId: Uuid,
    val deckId: Uuid,
    val lastReviewDate: Long?
) {
    companion object {
        fun createCardInDeck(cardId: Uuid, deckId: Uuid): CardInDeck {
            return CardInDeck(Uuid.random(), 100, 0, 0, 0, cardId, deckId, null)
        }

        fun calculatePriority(
            easyCount: Int,
            mediumCount: Int,
            hardCount: Int,
            lastReviewDate: Long?
        ): Int {
            val totalReviews = easyCount + mediumCount + hardCount

            // New cards get high priority (100) to ensure they're studied
            if (totalReviews == 0) return 100

            // Calculate difficulty ratio: harder cards get higher scores
            // hardCount weighted 3x, mediumCount weighted 1x
            val difficultyScore = (hardCount * 3.0 + mediumCount) / totalReviews
            val basePriority = (difficultyScore * 100).toInt()

            // Add time-based boost for cards not reviewed recently
            val daysSinceReview = lastReviewDate?.let {
                (Instant.now().toEpochMilli() - it) / (24 * 60 * 60 * 1000)
            } ?: 0

            // Cards not seen in 30+ days get priority boost
            val overdueBoost = when {
                daysSinceReview > 60 -> 50  // 2+ months: significant boost
                daysSinceReview > 30 -> 25  // 1+ month: moderate boost
                else -> 0
            }

            return maxOf(1, basePriority + overdueBoost)
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

    private fun generateTodaysStudyMaterial(numCardsToStudy: Int,
                                            repository: Repository): StudyDeckOfTheDay {
        val newCards = cardsInDeck.filter {
            it.cardInDeck.easyCount == 0 &&
                    it.cardInDeck.mediumCount == 0 &&
                    it.cardInDeck.hardCount == 0
        }
        val reviewedCards = cardsInDeck.filter {
            it.cardInDeck.easyCount > 0 ||
                    it.cardInDeck.mediumCount > 0 ||
                    it.cardInDeck.hardCount > 0
        }

        // Reserve slots for new cards (20% of total, minimum 10)
        val newCardSlots = maxOf(10, (numCardsToStudy * 0.2).toInt(), numCardsToStudy - reviewedCards.size)
        val reviewCardSlots = numCardsToStudy - newCardSlots

        // if (reviewedCardsSlots + newCardSlots)

        // Select new cards (shuffled to avoid order memorization)
        val selectedNewCards = newCards
            .sortedByDescending { it.cardInDeck.priority }
            .take(newCardSlots)
            .shuffled()

        // Select review cards by priority (shuffled within same bucket)
        val selectedReviewCards = reviewedCards
            .sortedByDescending { it.cardInDeck.priority }
            .take(reviewCardSlots)
            .shuffled()

        // If we don't have enough new cards, fill remaining slots with review cards
        val actualNewCards = selectedNewCards
        val additionalReviewCards = if (selectedNewCards.size < newCardSlots) {
            reviewedCards
                .sortedByDescending { it.cardInDeck.priority }
                .filter { it !in selectedReviewCards }
                .take(newCardSlots - selectedNewCards.size)
                .shuffled()
        } else {
            emptyList()
        }

        // Combine all selected cards
        val selectedCards = (actualNewCards + selectedReviewCards + additionalReviewCards)
            .shuffled()

        val resolvedToStudy = StudyDeckOfTheDay(
            Uuid.random(),
            deck.uuid,
            selectedCards.mapIndexed { index, card -> card.toInitialStudyCard(index) },
            false,
            DeckDate.getToday()
        )
        repository.upsertStudyDeck(resolvedToStudy.toStudyDeck(repository))
        return resolvedToStudy
    }
    fun generateStudyMaterial(toDeckManagement: () -> Unit,
                              numCardsToStudy: Int,
                              repository: Repository): Pair<HomePageDeckModel, StudyDeckOfTheDay> {
        val storedToStudy = repository.getStudyDeck(deck.uuid, DeckDate.getToday())
        val resolvedToStudy: StudyDeckOfTheDay
        if (storedToStudy == null || storedToStudy.studyDeck.completed && !storedToStudy.toStudyDeckOfTheDay().isTodaysDeck()) {
            resolvedToStudy = generateTodaysStudyMaterial(numCardsToStudy, repository)
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
            cardInDeck.easyCount == 0 && cardInDeck.mediumCount == 0 && cardInDeck.hardCount == 0,
            null,
            sortOrder
        )
    }
}