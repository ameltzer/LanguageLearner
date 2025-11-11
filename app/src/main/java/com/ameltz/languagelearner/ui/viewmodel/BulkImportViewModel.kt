package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.model.AnkiDeckImport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.Uuid


@HiltViewModel
class BulkImportViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    fun importAnkiDeck(ankiDeck: AnkiDeckImport) {
        var deck = repository.getDeck(ankiDeck.deckName)
        if (deck == null) {
            deck = CardInDeckAndDeckRelation(
                Deck(Uuid.random(), ankiDeck.deckName, Uuid.random()),
                cardsInDeck = emptyList()
            )
            repository.createDeck(deck)
        }

        ankiDeck.cards.forEach { card ->
            var appCard = card.toCard()
            try {
                repository.insertCard(appCard)
                appCard = repository.getCard(appCard.front, appCard.back)!!
                val deckCard = CardInDeck.createCardInDeck(appCard.uuid, deck.deck.uuid)
                repository.insertCardInDeck(deckCard)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}