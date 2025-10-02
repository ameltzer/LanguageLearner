package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.ameltz.languagelearner.data.entity.Deck

@Dao
interface DeckDao {
    @Query("SELECT * FROM deck")
    fun getAll(): List<Deck>

}