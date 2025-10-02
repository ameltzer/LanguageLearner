package com.ameltz.languagelearner.depinjection

import android.content.Context
import androidx.room.Room
import com.ameltz.languagelearner.data.AppDatabase
import com.ameltz.languagelearner.data.repository.DefaultRepository
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.data.dao.DeckDao
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
            "AppDatabase").build();
    }

    @Provides
    fun provideDeckDao(database:AppDatabase): DeckDao = database.deckDAO()

}