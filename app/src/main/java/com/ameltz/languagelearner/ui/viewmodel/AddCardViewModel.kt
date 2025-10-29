package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndCardRelation
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.Uuid

@HiltViewModel
class AddCardViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    fun addCard(cardInDecks: List<CardInDeck>, card: Card) {
        this.repository.upsertCard(card)
        this.repository.upsertAllCardInDecks(cardInDecks);
    }

    fun getAllDecks(): List<CardInDeckAndDeckRelation> {
        return this.repository.getAllDecks()
    }

    fun getCard(cardId: Uuid?): CardInDeckAndCardRelation? {
        if (cardId == null) {
            return null
        }
        return this.repository.getCardWithDecks(cardId)
    }

}