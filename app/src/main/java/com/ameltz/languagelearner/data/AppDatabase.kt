package com.ameltz.languagelearner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ameltz.languagelearner.data.dao.CardDao
import com.ameltz.languagelearner.data.dao.CardInDeckDao
import com.ameltz.languagelearner.data.dao.DeckDao
import com.ameltz.languagelearner.data.dao.SettingDao
import com.ameltz.languagelearner.data.dao.StudyCardDao
import com.ameltz.languagelearner.data.dao.StudyDeckDao
import com.ameltz.languagelearner.data.entity.DeckSettings
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.Setting
import com.ameltz.languagelearner.data.entity.StudyCard
import com.ameltz.languagelearner.data.entity.StudyDeck


@Database(entities = [DeckSettings::class, Card::class, Deck::class, CardInDeck::class,
    StudyDeck::class, StudyCard::class, Setting::class], version = 4)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDAO(): DeckDao
    abstract fun cardDAO(): CardDao
    abstract fun cardInDeckDao(): CardInDeckDao
    abstract fun studyDeckDao(): StudyDeckDao
    abstract fun studyCardDao(): StudyCardDao
    abstract fun settingDao(): SettingDao
}