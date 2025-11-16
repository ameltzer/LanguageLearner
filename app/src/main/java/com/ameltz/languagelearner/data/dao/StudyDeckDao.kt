package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ameltz.languagelearner.data.entity.StudyDeckWithCards
import com.ameltz.languagelearner.data.entity.StudyDeck
import kotlin.uuid.Uuid

@Dao
interface StudyDeckDao {

    // get deck
    @Query("SELECT * FROM studydeck where deckId = :deckId AND date = :date")
    fun getDeck(deckId: Uuid, date: Long): StudyDeckWithCards?

    @Query("SELECT * FROM studydeck where uuid = :studyDeckId")
    fun getDeck(studyDeckId: Uuid): StudyDeckWithCards?

    // create deck
    @Upsert
    fun upsertDeck(deck: StudyDeck)

}