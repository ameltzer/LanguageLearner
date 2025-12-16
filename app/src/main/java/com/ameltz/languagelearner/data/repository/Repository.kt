package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.CardInDeckWithCard
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.entity.Setting
import com.ameltz.languagelearner.data.entity.StudyCardWithCard
import com.ameltz.languagelearner.data.entity.StudyDeck
import com.ameltz.languagelearner.data.entity.StudyDeckWithCards
import com.ameltz.languagelearner.ui.model.StudyCardOfTheDay
import java.time.Instant
import java.util.Date
import kotlin.uuid.Uuid

interface Repository {

    fun getAllDecks(): List<CardInDeckAndDeckRelation>

    fun createDeck(deck: CardInDeckAndDeckRelation)

    fun updateDeck(deck: CardInDeckAndDeckRelation)
    fun updateDeck(deck: Deck)

    fun deleteDeck(deck:CardInDeckAndDeckRelation)

    fun getDeck(deckId: Uuid): CardInDeckAndDeckRelation?
    fun getDeck(deckName: String): CardInDeckAndDeckRelation?

    fun upsertCard(card: Card): Card

    fun doesCardExist(card: CardInDeckAndCardRelation): Boolean

    fun getCard(cardId: Uuid): Card?
    fun getCard(front: String, back: String): Card?

    fun getCardInDeck(front: String, back: String, deck: Uuid): CardInDeckWithCard?

    fun getCardWithDecks(cardId: Uuid): CardInDeckAndCardRelation?
    fun getAllCards(): List<Card>

    fun deleteCard(cardId: Uuid)


    fun doesCardInDeckExist(cardInDeck: CardInDeck): Boolean

    fun updateCardWithEasy(studyCardId: Uuid)
    fun updateCardWithMedium(studyCardId: Uuid)
    fun updateCardWithHard(studyCardId: Uuid)

    fun deleteCardinDeck(cardInDeck: CardInDeck)
    fun deleteCardInDeck(cardId: Uuid, deckId: Uuid)


    fun getStudyDeck(deckId: Uuid, instant: Instant): StudyDeckWithCards?
    fun getStudyDeck(studyDeckId: Uuid): StudyDeckWithCards?

    fun isDeckDone(studyDeckId: Uuid): Boolean

    fun doesStudyDeckExist(deckId: Uuid, instant: Instant): Boolean

    fun upsertStudyDeck(deck: StudyDeckWithCards)

    fun upsertStudyCard(currentCard: StudyCardWithCard, studyDeckId: Uuid)

    fun resetStudyDeckForStudy(deckId: Uuid)
    fun getHardTimeDelay(): Int
    fun getMediumTimeDelay(): Int
    fun saveMediumTimeDelay(mediumTimeDelay: Int)
    fun saveHardTimeDelay(hardTimeDelay: Int)
    fun getNumCardsToStudy(): Int
    fun saveNumCardsToStudy(numCards: Int)
    fun upsertAllCardInDecks(cardInDecks: List<CardInDeck>, updateIfExists: Boolean)
}
