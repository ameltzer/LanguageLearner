package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import kotlin.uuid.Uuid

interface Repository {

    fun getAllDecks(): List<CardInDeckAndDeckRelation>

    fun createDeck(deck: CardInDeckAndDeckRelation)

    fun updateDeck(deck: CardInDeckAndDeckRelation)

    fun deleteDeck(deck:CardInDeckAndDeckRelation)

    fun getDeck(deckId: Uuid): CardInDeckAndDeckRelation?

    fun upsertCard(card: Card)

    fun doesCardExist(card: CardInDeckAndCardRelation): Boolean

    fun getCard(cardId: Uuid): Card?

    fun getCardWithDecks(cardId: Uuid): CardInDeckAndCardRelation?
    fun getAllCards(): List<Card>

    fun updateCard(card: CardInDeckAndCardRelation)

    fun deleteCard(card: CardInDeckAndCardRelation)

    fun upsertAllCardInDecks(cardInDecks: List<CardInDeck>)

    fun doesCardInDeckExist(cardInDeck: CardInDeck): Boolean

    fun updateCardInDeck(cardInDeck: CardInDeck)

    fun deleteCardinDeck(cardInDeck: CardInDeck)

    fun insertCardInDeck(cardInDeck:CardInDeck)

}