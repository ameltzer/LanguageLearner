package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.ameltz.languagelearner.data.entity.StudyCard

@Dao
interface StudyCardDao {

    @Upsert
    fun upsertCard(card: StudyCard)

}