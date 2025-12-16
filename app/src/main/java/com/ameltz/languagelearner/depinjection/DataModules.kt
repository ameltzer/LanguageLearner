package com.ameltz.languagelearner.depinjection

import android.content.Context
import androidx.room.Room
import com.ameltz.languagelearner.data.AppDatabase
import com.ameltz.languagelearner.data.dao.CardDao
import com.ameltz.languagelearner.data.dao.CardInDeckDao
import com.ameltz.languagelearner.data.repository.DefaultRepository
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.data.dao.DeckDao
import com.ameltz.languagelearner.data.dao.SettingDao
import com.ameltz.languagelearner.data.dao.StudyCardDao
import com.ameltz.languagelearner.data.dao.StudyDeckDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindRepository(dataSource: DefaultRepository): Repository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
            "AppDatabase")
            .allowMainThreadQueries()
            .addMigrations(
                com.ameltz.languagelearner.data.MIGRATION_4_5,
                com.ameltz.languagelearner.data.MIGRATION_5_6
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDeckDao(database:AppDatabase): DeckDao = database.deckDAO()

    @Provides
    fun provideCardDao(database: AppDatabase): CardDao = database.cardDAO()

    @Provides
    fun provideCardInDeckDao(database: AppDatabase): CardInDeckDao = database.cardInDeckDao()

    @Provides
    fun provideStudyDeckDao(database: AppDatabase): StudyDeckDao = database.studyDeckDao()

    @Provides
    fun provideStudyCardDao(database: AppDatabase): StudyCardDao = database.studyCardDao()

    @Provides
    fun provideSettingDao(database: AppDatabase): SettingDao = database.settingDao()

}