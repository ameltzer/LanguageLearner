package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.dao.CardDao
import com.ameltz.languagelearner.data.dao.CardInDeckDao
import com.ameltz.languagelearner.data.dao.DeckDao
import com.ameltz.languagelearner.data.dao.SettingDao
import com.ameltz.languagelearner.data.dao.StudyCardDao
import com.ameltz.languagelearner.data.dao.StudyDeckDao
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.CardInDeckWithCard
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.entity.Setting
import com.ameltz.languagelearner.data.entity.StudyDeck
import com.ameltz.languagelearner.data.entity.StudyDeckWithCards
import com.ameltz.languagelearner.ui.model.StudyCardOfTheDay
import com.ameltz.languagelearner.ui.viewmodel.SettingsViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class DefaultRepository @Inject constructor(val deckDao: DeckDao,
                                            val cardDao: CardDao,
                                            val cardInDecksDao: CardInDeckDao,
                                            val studyDeckDao: StudyDeckDao,
                                            val studyCardDao: StudyCardDao,
                                            val settingDao: SettingDao
) : Repository {
    //Deck operations

    override fun getAllDecks(): List<CardInDeckAndDeckRelation> {
        return deckDao.getAll()
    }

    override fun createDeck(deck: CardInDeckAndDeckRelation) {
        deckDao.insertDeck(deck.deck)
    }

    override fun updateDeck(deck: CardInDeckAndDeckRelation) {
        deckDao.update(deck.deck)
    }

    override fun updateDeck(deck: Deck) {
        deckDao.update(deck)
    }

    override fun deleteDeck(deck: CardInDeckAndDeckRelation) {
        deckDao.deleteDeckTransactionally(deck, cardInDecksDao, cardDao)
    }

    override fun getDeck(deckId: Uuid): CardInDeckAndDeckRelation? {
        return deckDao.get(deckId)
    }

    override fun getDeck(deckName: String): CardInDeckAndDeckRelation? {
        return deckDao.get(deckName)
    }

    // Card operations
    override fun upsertCard(card: Card): Card {
        val existingCard = this.cardDao.getCard(card.front, card.back)
        if(existingCard != null) {
            return existingCard
        }
        cardDao.upsertCard(card)
        return card
    }

    override fun insertCard(card: Card) {
        cardDao.create(card)
    }

    override fun doesCardExist(card: CardInDeckAndCardRelation): Boolean {
        return this.cardDao.getCard(card.card.front, card.card.back) != null
    }

    override fun getCard(cardId: Uuid): Card? {
        return cardDao.getCard(cardId)
    }

    override fun getCard(
        front: String,
        back: String
    ): Card? {
        return cardDao.getCard(front, back)
    }

    override fun getCardInDeck(front: String, back: String, deck: Uuid): CardInDeckWithCard? {
        return cardInDecksDao.getSpecificCardInDeck(front, back, deck)
    }


    override fun getCardWithDecks(cardId: Uuid): CardInDeckAndCardRelation? {
        return cardDao.getCardWithDeck(cardId)
    }

    override fun getAllCards(): List<Card> {
        return cardDao.getAllCards()
    }

    override fun deleteCard(card: CardInDeckAndCardRelation) {
        cardDao.delete(card.card)
    }

    override fun deleteCard(cardId: Uuid) {
        val card = cardDao.getCardWithDeck(cardId)
        card?.instancesOfCard?.forEach { this.deleteCardinDeck(it) }
        cardDao.delete(cardId)
    }


    // Card in Deck operations
    override fun upsertAllCardInDecks(cardInDecks: List<CardInDeck>) {
        val cardInDeckToAdd = cardInDecks.filter { cardInDeck -> !doesCardInDeckExist(cardInDeck) }

        cardInDecksDao.upsertAll(cardInDecks.filter { cardInDeck ->  !doesCardInDeckExist(cardInDeck) })
    }

    override fun doesCardInDeckExist(cardInDeck: CardInDeck): Boolean {
        return this.cardInDecksDao.getSpecificCardInDeck(cardInDeck.cardId, cardInDeck.deckId) != null
    }

    override fun updateCardInDeck(cardInDeck: CardInDeck) {
        cardInDecksDao.update(cardInDeck)
    }

    override fun updateCardInDeckNextDay(studyCardId: Uuid) {
        val studyCard = studyCardDao.getCard(studyCardId)
        cardInDecksDao.updateCardInDeckNextDay(studyCard!!.cardInDeckId)
    }

    override fun deleteCardinDeck(cardInDeck: CardInDeck) {
        cardInDecksDao.deleteCardTransactionally(cardInDeck, cardDao)
    }

    override fun deleteCardInDeck(cardId: Uuid, deckId: Uuid) {
        val cardInDeck = cardInDecksDao.getSpecificCardInDeck(cardId, deckId)
        if (cardInDeck == null) {
            return
        }
        this.deleteCardinDeck(cardInDeck)
    }

    override fun insertCardInDeck(cardInDeck: CardInDeck) {
        cardInDecksDao.insertCardInDeck(cardInDeck)
    }

    override fun getStudyDeck(
        deckId: Uuid,
        instant: Instant
    ): StudyDeckWithCards? {
        val studyDeck = studyDeckDao.getDeck(deckId, instant.truncatedTo(ChronoUnit.DAYS).toEpochMilli())
        if (studyDeck == null) {
            return null
        }
        studyDeck.cards = studyDeck.cards.filter { card -> !card.studyCardOfTheDay.learned &&
                (
                        card.studyCardOfTheDay.lastAttempt == null ||
                        Instant.now().toEpochMilli() < card.studyCardOfTheDay.nextShowMinutes * (60 * 1000) + card.studyCardOfTheDay.lastAttempt
                )
        }
        return studyDeck
    }

    override fun isDeckDone(studyDeckId: Uuid): Boolean {
        val studyDeck = studyDeckDao.getDeck(studyDeckId)
        return studyDeck == null ||
                studyDeck.cards.isEmpty() ||
                studyDeck.cards
                    .map { card -> card.studyCardOfTheDay.learned }
                    .reduce { acc, isLearned -> acc && isLearned }
    }

    override fun doesStudyDeckExist(
        deckId: Uuid,
        instant: Instant
    ): Boolean {
        return studyDeckDao.getDeck(deckId, instant.truncatedTo(ChronoUnit.DAYS).toEpochMilli()) != null
    }

    override fun upsertStudyDeck(deck: StudyDeckWithCards) {
        studyDeckDao.upsertDeck(deck.studyDeck)
        deck.cards.forEach {
            studyCardDao.upsertCard(it.studyCardOfTheDay)
        }
    }

    override fun resetStudyDeckForStudy(deckId: Uuid) {
        studyDeckDao.resetDeckForStudy(deckId)
        studyCardDao.resetCardsLearnedStatus(deckId)

        // Randomize card order
        val cards = studyCardDao.getCardsForDeck(deckId)
        val shuffledIndices = cards.indices.shuffled()
        cards.forEachIndexed { index, card ->
            studyCardDao.updateCardSortOrder(card.uuid, shuffledIndices[index])
        }
    }

    override fun upsertStudyCard(currentCard: StudyCardOfTheDay, studyDeckId: Uuid) {
        studyCardDao.upsertCard(currentCard.toStudyCard(this, studyDeckId).studyCardOfTheDay)

    }

    // Settings operations
    override fun getMediumTimeDelay(): Int {
        val setting = settingDao.getSetting(SettingsViewModel.MEDIUM_TIME_DELAY_KEY) ?: Setting(SettingsViewModel.MEDIUM_TIME_DELAY_KEY, "15")
        return Integer.parseInt(setting.value)
    }

    override fun getHardTimeDelay(): Int {
        val setting = settingDao.getSetting(SettingsViewModel.HARD_TIME_DELAY_KEY) ?: Setting(SettingsViewModel.HARD_TIME_DELAY_KEY, "60")
        return Integer.parseInt(setting.value)
    }

    override fun saveMediumTimeDelay(mediumTimeDelay: Int) {
        settingDao.upsertSetting(Setting(SettingsViewModel.MEDIUM_TIME_DELAY_KEY, mediumTimeDelay.toString()))
    }

    override fun saveHardTimeDelay(hardTimeDelay: Int) {
        settingDao.upsertSetting(Setting(SettingsViewModel.HARD_TIME_DELAY_KEY, hardTimeDelay.toString()))
    }

}