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
    @Transaction
    abstract fun get(deckId: Uuid): CardInDeckAndDeckRelation?

    @Transaction
    @Query("SELECT * FROM deck WHERE name = :deckName")
    abstract fun get(deckName: String): CardInDeckAndDeckRelation?

    @Update
    abstract fun update(deck: Deck)

    @Delete
    protected abstract fun delete(deck: Deck)

    @Insert
    abstract fun insertDeck(deck: Deck)

    @Query("SELECT * FROM deck")
    @Transaction
    abstract fun getAll(): List<CardInDeckAndDeckRelation>

    open fun deleteDeckTransactionally(deckAndCards: CardInDeckAndDeckRelation, cardInDeckDao: CardInDeckDao, cardDao: CardDao) {
        // CASCADE will automatically delete all CardInDeck entries and their associated StudyCards
        delete(deckAndCards.deck)
    }



}