package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.Deck
import kotlin.uuid.Uuid

interface Repository {

    fun getAllDecks(): List<CardInDeckAndDeckRelation>

    fun createDeck(deck: CardInDeckAndDeckRelation)

    fun updateDeck(deck: CardInDeckAndDeckRelation)
    fun updateDeck(deck: Deck)

    fun deleteDeck(deck:CardInDeckAndDeckRelation)

    fun getDeck(deckId: Uuid): CardInDeckAndDeckRelation?

    fun upsertCard(card: Card): Card

    fun doesCardExist(card: CardInDeckAndCardRelation): Boolean

    fun getCard(cardId: Uuid): Card?

    fun getCardWithDecks(cardId: Uuid): CardInDeckAndCardRelation?
    fun getAllCards(): List<Card>

    fun updateCard(card: CardInDeckAndCardRelation)

    fun deleteCard(card: CardInDeckAndCardRelation)
    fun deleteCard(cardId: Uuid)

    fun upsertAllCardInDecks(cardInDecks: List<CardInDeck>)

    fun doesCardInDeckExist(cardInDeck: CardInDeck): Boolean

    fun updateCardInDeck(cardInDeck: CardInDeck)

    fun deleteCardinDeck(cardInDeck: CardInDeck)
    fun deleteCardInDeck(cardId: Uuid, deckId: Uuid)

    fun insertCardInDeck(cardInDeck:CardInDeck)

}