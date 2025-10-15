package com.ameltz.languagelearner.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.Deck
import java.util.UUID

interface CardDao {
    @Query("SELECT * FROM card WHERE front = :front AND back=:back")
    fun getCard(front: String, back:String): Card?

    @Query("SELECT * FROM card WHERE uuid = :cardId")
    fun getCard(cardId: UUID): Card?

    @Update
    fun update(card: Card)

    @Delete
    fun delete(card: Card)

    @Insert
    fun insertCard(card: Card)

}