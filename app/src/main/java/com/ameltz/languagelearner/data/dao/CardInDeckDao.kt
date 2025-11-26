package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckWithCard
import kotlin.uuid.Uuid

@Dao
abstract class CardInDeckDao {

    @Upsert
    abstract fun upsertAll(cardInDecks:List<CardInDeck>)

    @Query("SELECT * FROM CardInDeck cd INNER JOIN card ON card.uuid = cd.cardId WHERE card.uuid = :cardId AND cd.deckId = :deckId")
    abstract fun getSpecificCardInDeck(cardId: Uuid, deckId: Uuid): CardInDeck?

    @Query("SELECT * FROM CardInDeck cd INNER JOIN card ON card.uuid = cd.cardId WHERE card.front = :front AND card.back = :back AND cd.deckId = :deckId")
    abstract fun getSpecificCardInDeck(front: String, back: String, deckId: Uuid): CardInDeckWithCard?

    @Delete
    protected abstract fun delete(cardInDeck: CardInDeck)

    @Query("SELECT * FROM CardInDeck WHERE cardId = :cardId")
    abstract fun getAll(cardId: Uuid): List<CardInDeck>

    open fun deleteCardTransactionally(cardInDeck: CardInDeck, cardDao: CardDao) {
        delete(cardInDeck)
        val card = cardDao.getCard(cardInDeck.cardId)
        if (getAll(cardInDeck.cardId).isEmpty() && card != null) {
            cardDao.delete(card)
        }
    }

    @Query("UPDATE CardInDeck SET daysToNextShow = daysToNextShow + MAX(daysToNextShow * .2, 1) WHERE uuid = :cardInDeckId")
    abstract fun updateCardInDeckNextDay(cardInDeckId: Uuid)


}