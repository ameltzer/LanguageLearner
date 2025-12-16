package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
    @Transaction
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

    @Query("""
        UPDATE CardInDeck
        SET easyCount = easyCount + 1,
            lastReviewDate = :currentTime,
            priority = CASE
                WHEN (easyCount + 1 + mediumCount + hardCount) = 0 THEN 100
                ELSE MAX(1, (
                    CAST(((hardCount * 3.0 + mediumCount) / (easyCount + 1.0 + mediumCount + hardCount)) * 100 AS INTEGER) +
                    CASE
                        WHEN (:currentTime - COALESCE(lastReviewDate, :currentTime)) / (24 * 60 * 60 * 1000) > 60 THEN 50
                        WHEN (:currentTime - COALESCE(lastReviewDate, :currentTime)) / (24 * 60 * 60 * 1000) > 30 THEN 25
                        ELSE 0
                    END
                ))
            END
        WHERE uuid = :cardInDeckId
    """)
    abstract fun incrementEasyAndUpdatePriority(cardInDeckId: Uuid, currentTime: Long)

    @Query("""
        UPDATE CardInDeck
        SET mediumCount = mediumCount + 1,
            lastReviewDate = :currentTime,
            priority = CASE
                WHEN (easyCount + mediumCount + 1 + hardCount) = 0 THEN 100
                ELSE MAX(1, (
                    CAST(((hardCount * 3.0 + mediumCount + 1) / (easyCount + mediumCount + 1.0 + hardCount)) * 100 AS INTEGER) +
                    CASE
                        WHEN (:currentTime - COALESCE(lastReviewDate, :currentTime)) / (24 * 60 * 60 * 1000) > 60 THEN 50
                        WHEN (:currentTime - COALESCE(lastReviewDate, :currentTime)) / (24 * 60 * 60 * 1000) > 30 THEN 25
                        ELSE 0
                    END
                ))
            END
        WHERE uuid = :cardInDeckId
    """)
    abstract fun incrementMediumAndUpdatePriority(cardInDeckId: Uuid, currentTime: Long)

    @Query("""
        UPDATE CardInDeck
        SET hardCount = hardCount + 1,
            lastReviewDate = :currentTime,
            priority = CASE
                WHEN (easyCount + mediumCount + hardCount + 1) = 0 THEN 100
                ELSE MAX(1, (
                    CAST(((hardCount + 1) * 3.0 + mediumCount) / (easyCount + mediumCount + hardCount + 1.0) * 100 AS INTEGER) +
                    CASE
                        WHEN (:currentTime - COALESCE(lastReviewDate, :currentTime)) / (24 * 60 * 60 * 1000) > 60 THEN 50
                        WHEN (:currentTime - COALESCE(lastReviewDate, :currentTime)) / (24 * 60 * 60 * 1000) > 30 THEN 25
                        ELSE 0
                    END
                ))
            END
        WHERE uuid = :cardInDeckId
    """)
    abstract fun incrementHardAndUpdatePriority(cardInDeckId: Uuid, currentTime: Long)

    @Query("""
        SELECT * FROM CardInDeck
        WHERE hardCount > 0
        AND lastReviewDate IS NOT NULL
        AND lastReviewDate >= :cutoffTime
        AND deckId = :deckId
    """)
    @Transaction
    abstract fun getCardsMarkedHardSince(cutoffTime: Long, deckId: Uuid): List<CardInDeckWithCard>


}