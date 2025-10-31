package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import kotlin.uuid.Uuid

@Dao
interface CardDao {
    @Query("SELECT * FROM card WHERE front = :front AND back=:back")
    fun getCard(front: String, back:String): Card?

    @Query("SELECT * FROM card WHERE uuid = :cardId")
    fun getCard(cardId: Uuid): Card?

    @Query("SELECT * FROM card WHERE uuid = :cardId")
    fun getCardWithDeck(cardId: Uuid): CardInDeckAndCardRelation?

    @Update
    fun update(card: Card)

    @Delete
    fun delete(card: Card)

    @Query("DELETE FROM Card WHERE uuid = :cardId")
    fun delete(cardId: Uuid)

    @Upsert
    fun upsertCard(card: Card)

    @Query("SELECT * FROM card")
    fun getAllCards(): List<Card>

}