package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.CardInDeckWithCard
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.Uuid

@HiltViewModel
class DeckManagementViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    fun deleteDeck(deck: CardInDeckAndDeckRelation) {
        repository.deleteDeck(deck)
    }

    fun getDeck(deck: Uuid): CardInDeckAndDeckRelation? {
        return repository.getDeck(deck)
    }

    fun deleteCardInDeck(cardInDeck: CardInDeckWithCard) {
        repository.deleteCardinDeck(cardInDeck.cardInDeck)
    }

    fun saveDeck(deck: Deck) {
        repository.updateDeck(deck)
    }

    fun exportDeckToTSV(deck: CardInDeckAndDeckRelation): String {
        return deck.cardsInDeck.joinToString("\n") { cardInDeck ->
            "${cardInDeck.card.front}\t${cardInDeck.card.back}"
        }
    }
}