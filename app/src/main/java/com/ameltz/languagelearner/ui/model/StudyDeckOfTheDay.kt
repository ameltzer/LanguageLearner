package com.ameltz.languagelearner.ui.model

import com.ameltz.languagelearner.data.entity.StudyCard
import com.ameltz.languagelearner.data.entity.StudyCardWithCard
import com.ameltz.languagelearner.data.entity.StudyDeck
import com.ameltz.languagelearner.data.entity.StudyDeckWithCards
import com.ameltz.languagelearner.data.repository.Repository
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.uuid.Uuid

data class StudyDeckOfTheDay(val studyDeck: Uuid,
                             val deckId: Uuid,
                             var cards: List<StudyCardOfTheDay>,
                             val completed: Boolean,
                             val date: Instant) {
    fun toStudyDeck(repository: Repository): StudyDeckWithCards {
        return StudyDeckWithCards(
            StudyDeck(
                studyDeck,
                deckId,
                completed,
                date.truncatedTo(ChronoUnit.DAYS).toEpochMilli()
            ),
            cards.map { it.toStudyCard(repository, studyDeck) },
        )
    }
}

data class StudyCardOfTheDay(val front: String,
                             val back: String,
                             var learned: Boolean,
                             var nextShowMins: Int,
                             val deckId: Uuid,
                             val studyCardId: Uuid,
                             val isNewCard: Boolean,
                             var lastAttempt: Instant?
    ) {
    fun toStudyCard(repository: Repository, studyDeckId: Uuid): StudyCardWithCard {
        val cardInDeck = repository.getCardInDeck(front, back, deckId)!!
        return StudyCardWithCard(
            StudyCard(
                studyCardId,
                cardInDeck.cardInDeck.uuid,
                nextShowMins,
                learned,
                studyDeckId,
                isNewCard,
                lastAttempt?.toEpochMilli()
            ),
            cardInDeck
        )
    }
}