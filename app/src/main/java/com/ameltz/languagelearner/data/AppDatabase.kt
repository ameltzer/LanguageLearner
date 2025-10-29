package com.ameltz.languagelearner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ameltz.languagelearner.data.dao.CardDao
import com.ameltz.languagelearner.data.dao.CardInDeckDao
import com.ameltz.languagelearner.data.dao.DeckDao
import com.ameltz.languagelearner.data.entity.DeckSettings
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.entity.CardInDeck


@Database(entities = [DeckSettings::class, Card::class, Deck::class, CardInDeck::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDAO(): DeckDao
    abstract fun cardDAO(): CardDao
    abstract fun cardInDeckDao(): CardInDeckDao
}