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
class AddCardsToDeckViewModel  @Inject constructor(val repository: Repository) : ViewModel() {

    fun getDeck(deckId: Uuid): CardInDeckAndDeckRelation? {
        return repository.getDeck(deckId)
    }

    fun getCardsNotInDeck(deck: CardInDeckAndDeckRelation?) : List<Card> {
        if(deck == null) {
            return emptyList()
        }
        return repository.getAllCards()
    }

    fun addCardsToDeck(deckId: Uuid, cardIds: List<Uuid>) {
        repository.upsertAllCardInDecks(cardIds.map {
            cardId -> CardInDeck(Uuid.random(), 0, 0, cardId, deckId)
        })
    }

}