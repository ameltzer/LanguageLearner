package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.repository.Repository
import javax.inject.Inject

class AddCardViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    fun addCard(cardInDecks: List<CardInDeck>, card: Card) {
        this.repository.insertCard(card)
        this.repository.insertAllCardInDecks(cardInDecks);
    }

}