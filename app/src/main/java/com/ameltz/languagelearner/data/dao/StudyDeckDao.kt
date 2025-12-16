package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ameltz.languagelearner.data.entity.StudyDeckWithCards
import com.ameltz.languagelearner.data.entity.StudyDeck
import kotlin.uuid.Uuid

@Dao
interface StudyDeckDao {

    // get deck
    @Query("SELECT * FROM studydeck where deckId = :deckId AND date = :date")
    @Transaction
    fun getDeck(deckId: Uuid, date: Long): StudyDeckWithCards?

    @Query("SELECT * FROM studydeck where uuid = :studyDeckId")
    @Transaction
    fun getDeck(studyDeckId: Uuid): StudyDeckWithCards?

    // create deck
    @Upsert
    fun upsertDeck(deck: StudyDeck)

    @Query("UPDATE studydeck SET completed = false WHERE uuid = :deckId")
    fun resetDeckForStudy(deckId: Uuid)


}