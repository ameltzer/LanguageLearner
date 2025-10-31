package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.ameltz.languagelearner.data.entity.CardInDeck
import kotlin.uuid.Uuid

@Dao
abstract class CardInDeckDao {

    @Upsert
    abstract fun upsertAll(cardInDecks:List<CardInDeck>)

    @Query("SELECT * FROM CardInDeck cd INNER JOIN card ON card.uuid = cd.cardId WHERE card.uuid = :cardId AND cd.deckId = :deckId")
    abstract fun getSpecificCardInDeck(cardId: Uuid, deckId: Uuid): CardInDeck?

    @Update
    abstract fun update(cardInDeck: CardInDeck)

    @Delete
    protected abstract fun delete(cardInDeck: CardInDeck)

    @Insert
    abstract fun insertCardInDeck(cardInDeck: CardInDeck)

    @Query("SELECT * FROM CardInDeck WHERE cardId = :cardId")
    abstract fun getAll(cardId: Uuid): List<CardInDeck>

    open fun deleteCardTransactionally(cardInDeck: CardInDeck, cardDao: CardDao) {
        delete(cardInDeck)
        val card = cardDao.getCard(cardInDeck.cardId)
        if (getAll(cardInDeck.cardId).isEmpty() && card != null) {
            cardDao.delete(card)
        }
    }



}