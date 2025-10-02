package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomePageViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    fun getAllDeckSummaries(): List<Deck> {
        return this.repository.getAllDecks();
    }

}