package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.Uuid

@HiltViewModel
class CardManagementViewModel  @Inject constructor(val repository: Repository) : ViewModel() {

    fun getDeck(deckId: Uuid): CardInDeckAndDeckRelation? {
        return repository.getDeck(deckId)
    }

    fun getAllCards() : List<Card> {
        return repository.getAllCards()
    }

    fun addCardsToDeck(deckId: Uuid, cardIds: List<Uuid>) {
        repository.upsertAllCardInDecks(cardIds.map {
            cardId -> CardInDeck.createCardInDeck(cardId, deckId)
        }, true)
    }

    fun removeCardsFromDeck(deckId: Uuid, cardsToRemove: List<Uuid>) {
        cardsToRemove.forEach { cardId ->
            repository.deleteCardInDeck(cardId, deckId)
        }

    }

}