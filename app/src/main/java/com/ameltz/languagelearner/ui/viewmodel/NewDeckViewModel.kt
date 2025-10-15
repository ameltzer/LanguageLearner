package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewDeckViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    fun createNewDeck(deck: Deck) {
        return this.repository.createDeck(deck)
    }

}