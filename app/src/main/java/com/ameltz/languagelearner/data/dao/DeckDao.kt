package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.Deck
import kotlin.uuid.Uuid

@Dao
abstract class DeckDao {

    @Query("SELECT * FROM deck WHERE uuid = :deckId")
    abstract fun get(deckId: Uuid): CardInDeckAndDeckRelation?

    @Update
    abstract fun update(deck: Deck)

    @Delete
    protected abstract fun delete(deck: Deck)

    @Insert
    abstract fun insertDeck(deck: Deck)

    @Query("SELECT * FROM deck")
    abstract fun getAll(): List<CardInDeckAndDeckRelation>

    open fun deleteDeckTransactionally(deckAndCards: CardInDeckAndDeckRelation, cardInDeckDao: CardInDeckDao, cardDao: CardDao) {
        deckAndCards.cardsInDeck.forEach { cardInDeck -> cardInDeckDao.deleteCardTransactionally(cardInDeck.cardInDeck, cardDao) }
        delete(deckAndCards.deck)
    }



}