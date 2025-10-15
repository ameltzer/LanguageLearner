package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.entity.DeckAndDeckSettingsRelation
import java.util.UUID
import javax.inject.Inject

@Dao
abstract class DeckDao @Inject constructor(val cardInDeckDao: CardInDeckDao){

    @Query("SELECT * FROM deck WHERE uuid = :deckId")
    abstract fun get(deckId: UUID): CardInDeckAndDeckRelation?

    @Update
    abstract fun update(deck: Deck)

    @Delete
    protected abstract fun delete(deck: Deck)

    @Insert
    abstract fun insertDeck(deck: Deck)

    @Query("SELECT * FROM deck")
    abstract fun getAll(): List<CardInDeckAndDeckRelation>

    @Transaction
    fun deleteDeckTransactionally(deckAndCards: CardInDeckAndDeckRelation) {
        delete(deckAndCards.deck)
        deckAndCards.cardsInDeck.forEach { cardInDeck -> cardInDeckDao.deleteCardTransactionally(cardInDeck) }
    }



}