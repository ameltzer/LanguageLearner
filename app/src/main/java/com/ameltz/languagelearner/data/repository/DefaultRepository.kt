package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.dao.DeckDao
import com.ameltz.languagelearner.data.entity.Deck
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRepository @Inject constructor(val deckDao: DeckDao) : Repository {
    override fun getAllDecks(): List<Deck> {
        return deckDao.getAll();
    }
}