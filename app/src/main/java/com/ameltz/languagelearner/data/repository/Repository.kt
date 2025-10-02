package com.ameltz.languagelearner.data.repository

import com.ameltz.languagelearner.data.entity.Deck

interface Repository {

    fun getAllDecks(): List<Deck>

}