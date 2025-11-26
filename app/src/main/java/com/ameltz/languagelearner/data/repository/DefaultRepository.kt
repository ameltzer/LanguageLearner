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
import com.ameltz.languagelearner.data.entity.StudyCard
import com.ameltz.languagelearner.data.entity.StudyCardWithCard
import com.ameltz.languagelearner.data.entity.StudyDeckWithCards
import com.ameltz.languagelearner.ui.viewmodel.SettingsViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
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
        println("[Repository] getAllDecks() called")
        val result = deckDao.getAll()
        println("[Repository] getAllDecks() -> returned ${result.size} decks")
        return result
    }

    override fun createDeck(deck: CardInDeckAndDeckRelation) {
        println("[Repository] createDeck() called with deck: ${deck.deck.name} (${deck.deck.uuid})")
        deckDao.insertDeck(deck.deck)
        println("[Repository] createDeck() -> completed")
    }

    override fun updateDeck(deck: CardInDeckAndDeckRelation) {
        println("[Repository] updateDeck(CardInDeckAndDeckRelation) called with deck: ${deck.deck.name}")
        deckDao.update(deck.deck)
        println("[Repository] updateDeck(CardInDeckAndDeckRelation) -> completed")
    }

    override fun updateDeck(deck: Deck) {
        println("[Repository] updateDeck(Deck) called with deck: ${deck.name} (${deck.uuid})")
        deckDao.update(deck)
        println("[Repository] updateDeck(Deck) -> completed")
    }

    override fun deleteDeck(deck: CardInDeckAndDeckRelation) {
        println("[Repository] deleteDeck() called with deck: ${deck.deck.name} (${deck.deck.uuid})")
        deckDao.deleteDeckTransactionally(deck, cardInDecksDao, cardDao)
        println("[Repository] deleteDeck() -> completed")
    }

    override fun getDeck(deckId: Uuid): CardInDeckAndDeckRelation? {
        println("[Repository] getDeck(deckId) called with deckId: $deckId")
        val result = deckDao.get(deckId)
        println("[Repository] getDeck(deckId) -> ${result?.deck?.name ?: "null"}")
        return result
    }

    override fun getDeck(deckName: String): CardInDeckAndDeckRelation? {
        println("[Repository] getDeck(deckName) called with deckName: $deckName")
        val result = deckDao.get(deckName)
        println("[Repository] getDeck(deckName) -> ${if (result != null) "found" else "null"}")
        return result
    }

    // Card operations
    override fun upsertCard(card: Card): Card {
        println("[Repository] upsertCard() called with front: '${card.front}', back: '${card.back}'")
        val existingCard = this.cardDao.getCard(card.front, card.back)
        if(existingCard != null) {
            println("[Repository] upsertCard() -> card already exists, returning existing card ${existingCard.uuid}")
            return existingCard
        }
        cardDao.upsertCard(card)
        println("[Repository] upsertCard() -> inserted new card ${card.uuid}")
        return card
    }


    override fun doesCardExist(card: CardInDeckAndCardRelation): Boolean {
        println("[Repository] doesCardExist() called with front: '${card.card.front}'")
        val result = this.cardDao.getCard(card.card.front, card.card.back) != null
        println("[Repository] doesCardExist() -> $result")
        return result
    }

    override fun getCard(cardId: Uuid): Card? {
        println("[Repository] getCard(cardId) called with cardId: $cardId")
        val result = cardDao.getCard(cardId)
        println("[Repository] getCard(cardId) -> ${result?.uuid ?: "null"}")
        return result
    }

    override fun getCard(
        front: String,
        back: String
    ): Card? {
        println("[Repository] getCard(front, back) called with front: '$front', back: '$back'")
        val result = cardDao.getCard(front, back)
        println("[Repository] getCard(front, back) -> ${result?.uuid ?: "null"}")
        return result
    }

    override fun getCardInDeck(front: String, back: String, deck: Uuid): CardInDeckWithCard? {
        println("[Repository] getCardInDeck() called with front: '$front', back: '$back', deck: $deck")
        val result = cardInDecksDao.getSpecificCardInDeck(front, back, deck)
        println("[Repository] getCardInDeck() -> ${if (result != null) "found" else "null"}")
        return result
    }


    override fun getCardWithDecks(cardId: Uuid): CardInDeckAndCardRelation? {
        println("[Repository] getCardWithDecks() called with cardId: $cardId")
        val result = cardDao.getCardWithDeck(cardId)
        println("[Repository] getCardWithDecks() -> found ${result?.instancesOfCard?.size ?: 0} deck associations")
        return result
    }

    override fun getAllCards(): List<Card> {
        println("[Repository] getAllCards() called")
        val result = cardDao.getAllCards()
        println("[Repository] getAllCards() -> returned ${result.size} cards")
        return result
    }

    override fun deleteCard(cardId: Uuid) {
        println("[Repository] deleteCard() called with cardId: $cardId")
        val card = cardDao.getCardWithDeck(cardId)
        card?.instancesOfCard?.forEach { this.deleteCardinDeck(it) }
        cardDao.delete(cardId)
        println("[Repository] deleteCard() -> completed")
    }


    // Card in Deck operations
    override fun upsertAllCardInDecks(cardInDecks: List<CardInDeck>, updateIfExists: Boolean) {
        println("[Repository] upsertAllCardInDecks() called with ${cardInDecks.size} cards, updateIfExists: $updateIfExists")
        val doesExist = cardInDecks.groupBy { doesCardInDeckExist(it) }
        cardInDecksDao.upsertAll(doesExist[true] ?: emptyList())
        cardInDecksDao.upsertAll(doesExist[false] ?: emptyList())

        val toUpdate = doesExist[true]?.mapNotNull { cardInDeck ->
            cardInDecksDao.getSpecificCardInDeck(
                cardInDeck.cardId,
                cardInDeck.deckId
            )
        } ?: emptyList()
        val toInsert = doesExist[false]?.mapNotNull {
                cardInDeck ->
                cardInDecksDao.getSpecificCardInDeck(
                cardInDeck.cardId,
                cardInDeck.deckId
            )
        } ?: emptyList()

        val maxCardInStudyDeck = this.getNumCardsToStudy()

        // Add newly inserted cards to today's study deck if it exists
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS)
        toInsert.groupBy { it.deckId }.forEach { (deckId, cardsInDeck) ->
            val studyDeck = studyDeckDao.getDeck(deckId, today.toEpochMilli())
            if (studyDeck != null && studyDeck.cards.size < maxCardInStudyDeck) {
                val numCardsToAdd = maxCardInStudyDeck - studyDeck.cards.size
                println("[Repository] upsertAllCardInDecks() -> adding ${numCardsToAdd} cards to today's study deck for deck $deckId")
                val maxOrder = studyCardDao.getMaxSortOrder(studyDeck.studyDeck.uuid) ?: -1
                cardsInDeck.take(numCardsToAdd).forEachIndexed { index, cardInDeck ->
                    val studyCard = StudyCard(
                        uuid = Uuid.random(),
                        cardInDeckId = cardInDeck.uuid,
                        nextShowMinutes = 0,
                        learned = false,
                        studyDeck = studyDeck.studyDeck.uuid,
                        isNewCard = cardInDeck.daysToNextShow == 0,
                        lastAttempt = null,
                        sortOrder = maxOrder + 1 + index
                    )

                    studyCardDao.upsertCard(studyCard)
                }
            } else {
                println("[Repository] upsertAllCardInDecks() -> today's study deck does not exist or is full for deck $deckId")
            }
        }
        println("[Repository] upsertAllCardInDecks() -> completed")
    }

    override fun doesCardInDeckExist(cardInDeck: CardInDeck): Boolean {
        val result = this.cardInDecksDao.getSpecificCardInDeck(cardInDeck.cardId, cardInDeck.deckId) != null
        return result
    }


    override fun updateCardInDeckNextDay(studyCardId: Uuid) {
        println("[Repository] updateCardInDeckNextDay() called with studyCardId: $studyCardId")
        val studyCard = studyCardDao.getCard(studyCardId)
        cardInDecksDao.updateCardInDeckNextDay(studyCard!!.cardInDeckId)
        println("[Repository] updateCardInDeckNextDay() -> completed")
    }

    override fun deleteCardinDeck(cardInDeck: CardInDeck) {
        println("[Repository] deleteCardinDeck() called with cardInDeck: ${cardInDeck.uuid}")
        cardInDecksDao.deleteCardTransactionally(cardInDeck, cardDao)
        println("[Repository] deleteCardinDeck() -> completed")
    }

    override fun deleteCardInDeck(cardId: Uuid, deckId: Uuid) {
        println("[Repository] deleteCardInDeck() called with cardId: $cardId, deckId: $deckId")
        val cardInDeck = cardInDecksDao.getSpecificCardInDeck(cardId, deckId)
        if (cardInDeck == null) {
            println("[Repository] deleteCardInDeck() -> card not found")
            return
        }
        this.deleteCardinDeck(cardInDeck)
        println("[Repository] deleteCardInDeck() -> completed")
    }

    override fun getStudyDeck(
        deckId: Uuid,
        instant: Instant
    ): StudyDeckWithCards? {
        println("[Repository] getStudyDeck(deckId, instant) called with deckId: $deckId, instant: $instant")
        val studyDeck = studyDeckDao.getDeck(deckId, instant.truncatedTo(ChronoUnit.DAYS).toEpochMilli())
        val result = processStudyDeck(studyDeck)
        println("[Repository] getStudyDeck(deckId, instant) -> ${result?.cards?.size ?: 0} cards ready to study")
        return result

    }

    override fun getStudyDeck(studyDeckId: Uuid): StudyDeckWithCards? {
        println("[Repository] getStudyDeck(studyDeckId) called with studyDeckId: $studyDeckId")
        val studyDeck = studyDeckDao.getDeck(studyDeckId)
        val result = processStudyDeck(studyDeck)
        println("[Repository] getStudyDeck(studyDeckId) -> ${result?.cards?.size ?: 0} cards ready to study")
        return result
    }

    private fun processStudyDeck(studyDeck: StudyDeckWithCards?): StudyDeckWithCards? {
        if (studyDeck == null) {
            return null
        }
        val originalSize = studyDeck.cards.size
        studyDeck.cards = studyDeck.cards.filter { card ->
            val nextShowTime = if(card.studyCardOfTheDay.lastAttempt != null) {(card.studyCardOfTheDay.nextShowMinutes * (60 * 1000) + card.studyCardOfTheDay.lastAttempt!!) } else { null }
            !card.studyCardOfTheDay.learned &&
                    (
                            nextShowTime == null ||
                                    Instant.now().toEpochMilli() > nextShowTime
                            )
        }
        println("[Repository] processStudyDeck() -> filtered from $originalSize to ${studyDeck.cards.size} cards")
        return studyDeck
    }

    override fun isDeckDone(studyDeckId: Uuid): Boolean {
        println("[Repository] isDeckDone() called with studyDeckId: $studyDeckId")
        val studyDeck = studyDeckDao.getDeck(studyDeckId)
        val result = studyDeck == null ||
                studyDeck.cards.isEmpty() ||
                studyDeck.cards
                    .map { card -> card.studyCardOfTheDay.learned }
                    .reduce { acc, isLearned -> acc && isLearned }
        println("[Repository] isDeckDone() -> $result")
        return result
    }

    override fun doesStudyDeckExist(
        deckId: Uuid,
        instant: Instant
    ): Boolean {
        println("[Repository] doesStudyDeckExist() called with deckId: $deckId, instant: $instant")
        val result = studyDeckDao.getDeck(deckId, instant.truncatedTo(ChronoUnit.DAYS).toEpochMilli()) != null
        println("[Repository] doesStudyDeckExist() -> $result")
        return result
    }

    override fun upsertStudyDeck(deck: StudyDeckWithCards) {
        println("[Repository] upsertStudyDeck() called with ${deck.cards.size} cards")
        studyDeckDao.upsertDeck(deck.studyDeck)
        deck.cards.forEach {
            studyCardDao.upsertCard(it.studyCardOfTheDay)
        }
        println("[Repository] upsertStudyDeck() -> completed")
    }

    override fun resetStudyDeckForStudy(deckId: Uuid) {
        println("[Repository] resetStudyDeckForStudy() called with deckId: $deckId")
        studyDeckDao.resetDeckForStudy(deckId)
        studyCardDao.resetCardsLearnedStatus(deckId)

        // Randomize card order
        val cards = studyCardDao.getCardsForDeck(deckId)
        println("[Repository] resetStudyDeckForStudy() -> shuffling ${cards.size} cards")
        val shuffledIndices = cards.indices.shuffled()
        cards.forEachIndexed { index, card ->
            studyCardDao.updateCardSortOrder(card.uuid, shuffledIndices[index])
        }
        println("[Repository] resetStudyDeckForStudy() -> completed")
    }

    override fun upsertStudyCard(currentCard: StudyCardWithCard, studyDeckId: Uuid) {
        println("[Repository] upsertStudyCard() called with studyCardId: ${currentCard.studyCardOfTheDay.uuid}")
        studyCardDao.upsertCard(currentCard.studyCardOfTheDay)
        println("[Repository] upsertStudyCard() -> completed")
    }

    // Settings operations
    override fun getMediumTimeDelay(): Int {
        println("[Repository] getMediumTimeDelay() called")
        val setting = settingDao.getSetting(SettingsViewModel.MEDIUM_TIME_DELAY_KEY) ?: Setting(SettingsViewModel.MEDIUM_TIME_DELAY_KEY, "60")
        val result = Integer.parseInt(setting.value)
        println("[Repository] getMediumTimeDelay() -> $result minutes")
        return result
    }

    override fun getHardTimeDelay(): Int {
        println("[Repository] getHardTimeDelay() called")
        val setting = settingDao.getSetting(SettingsViewModel.HARD_TIME_DELAY_KEY) ?: Setting(SettingsViewModel.HARD_TIME_DELAY_KEY, "15")
        val result = Integer.parseInt(setting.value)
        println("[Repository] getHardTimeDelay() -> $result minutes")
        return result
    }

    override fun saveMediumTimeDelay(mediumTimeDelay: Int) {
        println("[Repository] saveMediumTimeDelay() called with value: $mediumTimeDelay")
        settingDao.upsertSetting(Setting(SettingsViewModel.MEDIUM_TIME_DELAY_KEY, mediumTimeDelay.toString()))
        println("[Repository] saveMediumTimeDelay() -> completed")
    }

    override fun saveHardTimeDelay(hardTimeDelay: Int) {
        println("[Repository] saveHardTimeDelay() called with value: $hardTimeDelay")
        settingDao.upsertSetting(Setting(SettingsViewModel.HARD_TIME_DELAY_KEY, hardTimeDelay.toString()))
        println("[Repository] saveHardTimeDelay() -> completed")
    }

    override fun getNumCardsToStudy(): Int {
        println("[Repository] getNumCardsToStudy() called")
        val setting = settingDao.getSetting(SettingsViewModel.NUM_CARDS_TO_STUDY_KEY) ?: Setting(SettingsViewModel.NUM_CARDS_TO_STUDY_KEY, "50")
        val result = Integer.parseInt(setting.value)
        println("[Repository] getNumCardsToStudy() -> $result cards")
        return result
    }

    override fun saveNumCardsToStudy(numCards: Int) {
        println("[Repository] saveNumCardsToStudy() called with value: $numCards")
        settingDao.upsertSetting(Setting(SettingsViewModel.NUM_CARDS_TO_STUDY_KEY, numCards.toString()))
        println("[Repository] saveNumCardsToStudy() -> completed")
    }

}