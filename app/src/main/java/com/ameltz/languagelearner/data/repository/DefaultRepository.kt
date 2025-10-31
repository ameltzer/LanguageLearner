package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.dao.CardDao
import com.ameltz.languagelearner.data.dao.CardInDeckDao
import com.ameltz.languagelearner.data.dao.DeckDao
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.Deck
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class DefaultRepository @Inject constructor(val deckDao: DeckDao,
                                            val cardDao: CardDao,
                                            val cardInDecksDao: CardInDeckDao,) : Repository {
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

    // Card operations
    override fun upsertCard(card: Card): Card {
        val existingCard = this.cardDao.getCard(card.front, card.back)
        if(existingCard != null) {
            return existingCard;
        }
        cardDao.upsertCard(card)
        return card
    }

    override fun doesCardExist(card: CardInDeckAndCardRelation): Boolean {
        return this.cardDao.getCard(card.card.front, card.card.back) != null
    }

    override fun getCard(cardId: Uuid): Card? {
        return cardDao.getCard(cardId)
    }

    override fun getCardWithDecks(cardId: Uuid): CardInDeckAndCardRelation? {
        return cardDao.getCardWithDeck(cardId)
    }

    override fun getAllCards(): List<Card> {
        return cardDao.getAllCards()
    }

    override fun updateCard(card: CardInDeckAndCardRelation) {
        cardDao.update(card.card)
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
}