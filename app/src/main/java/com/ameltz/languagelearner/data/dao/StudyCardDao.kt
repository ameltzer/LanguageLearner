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

}