package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ameltz.languagelearner.data.entity.StudyCard
import kotlin.uuid.Uuid

@Dao
interface StudyCardDao {

    @Upsert
    fun upsertCard(card: StudyCard)

    @Query("SELECT * FROM studycard WHERE uuid = :uuid")
    fun getCard(uuid: Uuid): StudyCard?

    @Query("SELECT * FROM studycard WHERE studyDeck = :deckId")
    fun getCardsForDeck(deckId: Uuid): List<StudyCard>

    @Query("UPDATE studycard SET learned = false WHERE studyDeck = :deckId")
    fun resetCardsLearnedStatus(deckId: Uuid)

    @Query("UPDATE studycard SET sortOrder = :sortOrder WHERE uuid = :cardId")
    fun updateCardSortOrder(cardId: Uuid, sortOrder: Int)

}