package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.dao.CardDao
import com.ameltz.languagelearner.data.dao.CardInDeckDao
import com.ameltz.languagelearner.data.dao.DeckDao
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.Deck
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRepository @Inject constructor(val deckDao: DeckDao,
                                            val cardDao: CardDao,
                                            val cardInDecksDao: CardInDeckDao) : Repository {
    //Deck operations
    override fun getAllDecks(): List<CardInDeckAndDeckRelation> {
        return deckDao.getAll();
    }

    override fun createDeck(deck: CardInDeckAndDeckRelation) {
        deckDao.insertDeck(deck.deck)
    }

    override fun updateDeck(deck: CardInDeckAndDeckRelation) {
        deckDao.update(deck.deck)
    }

    override fun deleteDeck(deck: CardInDeckAndDeckRelation) {
        deckDao.deleteDeckTransactionally(deck)
    }

    override fun getDeck(deckId: UUID): CardInDeckAndDeckRelation? {
        return deckDao.get(deckId)
    }

    // Card operations
    override fun insertCard(card: CardInDeckAndCardRelation) {
        if(!this.doesCardExist(card)) {
            this.insertCard(card)
        }
    }

    override fun doesCardExist(card: CardInDeckAndCardRelation): Boolean {
        return this.cardDao.getCard(card.card.front, card.card.back) != null
    }

    override fun getCard(cardId: UUID): Card? {
        return cardDao.getCard(cardId)
    }

    override fun updateCard(card: CardInDeckAndCardRelation) {
        cardDao.update(card.card)
    }

    override fun deleteCard(card: CardInDeckAndCardRelation) {
        cardDao.delete(card.card)
    }

    // Card in Deck operations
    override fun insertAllCardInDecks(cardInDecks: List<CardInDeck>) {
        cardInDecksDao.insertAll(cardInDecks.filter { cardInDeck ->  doesCardInDeckExist(cardInDeck) })
    }

    override fun doesCardInDeckExist(cardInDeck: CardInDeck): Boolean {
        return this.cardInDecksDao.getSpecificCardInDeck(cardInDeck.cardId, cardInDeck.deckId) != null
    }

    override fun updateCardInDeck(cardInDeck: CardInDeck) {
        cardInDecksDao.update(cardInDeck)
    }

    override fun deleteCardinDeck(cardInDeck: CardInDeck) {
        cardInDecksDao.deleteCardTransactionally(cardInDeck)
    }

    override fun insertCardInDeck(cardInDeck: CardInDeck) {
        cardInDecksDao.insertCardInDeck(cardInDeck)
    }
}