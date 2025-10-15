package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.dao.CardInDeckDao
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.Deck
import java.util.UUID

interface Repository {

    fun getAllDecks(): List<CardInDeckAndDeckRelation>

    fun createDeck(deck: CardInDeckAndDeckRelation)

    fun updateDeck(deck: CardInDeckAndDeckRelation)

    fun deleteDeck(deck:CardInDeckAndDeckRelation)

    fun getDeck(deckId: UUID): CardInDeckAndDeckRelation?

    fun insertCard(card: CardInDeckAndCardRelation)

    fun doesCardExist(card: CardInDeckAndCardRelation): Boolean

    fun getCard(cardId: UUID): Card?

    fun updateCard(card: CardInDeckAndCardRelation)

    fun deleteCard(card: CardInDeckAndCardRelation)

    fun insertAllCardInDecks(cardInDecks: List<CardInDeck>)

    fun doesCardInDeckExist(cardInDeck: CardInDeck): Boolean

    fun updateCardInDeck(cardInDeck: CardInDeck)

    fun deleteCardinDeck(cardInDeck: CardInDeck)

    fun insertCardInDeck(cardInDeck:CardInDeck)

}