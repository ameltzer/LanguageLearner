package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import com.ameltz.languagelearner.ui.model.StudyDeckCardView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.Uuid

@HiltViewModel
class HomePageViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    fun getAllDeckSummaries(toManageDeck: (deckId: Uuid) -> Unit): List<HomePageDeckModel> {
        println("[HomePageViewModel] getAllDeckSummaries() called")
        val studyMaterial = this.repository.getAllDecks().map { dbDeck ->
            println("[HomePageViewModel] getAllDeckSummaries() -> generating study material for deck: ${dbDeck.deck.name}")
            dbDeck.generateStudyMaterial(
                {toManageDeck(dbDeck.deck.uuid)},
                repository.getNumCardsToStudy(),
                repository
            )
        }
        println("[HomePageViewModel] getAllDeckSummaries() -> returning ${studyMaterial.size} deck summaries")
        return studyMaterial.map { it.first }
    }

    fun resetDeckForStudy(deckId: Uuid) {
        println("[HomePageViewModel] resetDeckForStudy() called with deckId: $deckId")
        repository.resetStudyDeckForStudy(deckId)
        println("[HomePageViewModel] resetDeckForStudy() -> completed")
    }

    fun createHardCardsDeck(sourceDeckId: Uuid, sourceDeckName: String): String? {
        println("[HomePageViewModel] createHardCardsDeck() called with sourceDeckId: $sourceDeckId")
        val lookbackDays = repository.getHardCardsLookbackDays()
        val hardCards = repository.getCardsMarkedHardInLastXDays(lookbackDays, sourceDeckId)

        if (hardCards.isEmpty()) {
            println("[HomePageViewModel] createHardCardsDeck() -> no hard cards found")
            return null
        }

        val deckName = "$sourceDeckName - Hard Cards (Last $lookbackDays Days)"
        repository.createHardCardsDeck(deckName, lookbackDays, sourceDeckId)
        println("[HomePageViewModel] createHardCardsDeck() -> created deck: $deckName")
        return deckName
    }

    fun deleteDeck(deckId: Uuid) {
        println("[HomePageViewModel] deleteDeck() called with deckId: $deckId")
        val deck = repository.getDeck(deckId)
        if (deck != null) {
            repository.deleteDeck(deck)
            println("[HomePageViewModel] deleteDeck() -> completed")
        } else {
            println("[HomePageViewModel] deleteDeck() -> deck not found")
        }
    }

    fun getFullStudyDeckCards(studyDeckId: Uuid): List<StudyDeckCardView> {
        println("[HomePageViewModel] getStudyDeckCards() called with studyDeckId: $studyDeckId")
        val studyDeck = repository.getFullStudyDeck(studyDeckId)
        if (studyDeck == null) {
            println("[HomePageViewModel] getStudyDeckCards() -> study deck not found")
            return emptyList()
        }

        val cards = studyDeck.cards.map { studyCardWithCard ->
            StudyDeckCardView(
                front = studyCardWithCard.cardInDeck.card.front,
                back = studyCardWithCard.cardInDeck.card.back,
                easyCount = studyCardWithCard.cardInDeck.cardInDeck.easyCount,
                mediumCount = studyCardWithCard.cardInDeck.cardInDeck.mediumCount,
                hardCount = studyCardWithCard.cardInDeck.cardInDeck.hardCount,
                isLearned = studyCardWithCard.studyCardOfTheDay.learned
            )
        }

        // Sort: unfinished cards first, then alphabetically by front
        val sortedCards = cards.sortedWith(
            compareBy<StudyDeckCardView> { it.isLearned }
                .thenBy { it.front }
        )

        println("[HomePageViewModel] getStudyDeckCards() -> returning ${sortedCards.size} cards")
        return sortedCards
    }
    fun getStudyDeckCards(studyDeckId: Uuid): List<StudyDeckCardView> {
        println("[HomePageViewModel] getStudyDeckCards() called with studyDeckId: $studyDeckId")
        val studyDeck = repository.getStudyDeck(studyDeckId)
        if (studyDeck == null) {
            println("[HomePageViewModel] getStudyDeckCards() -> study deck not found")
            return emptyList()
        }

        val cards = studyDeck.cards.map { studyCardWithCard ->
            StudyDeckCardView(
                front = studyCardWithCard.cardInDeck.card.front,
                back = studyCardWithCard.cardInDeck.card.back,
                easyCount = studyCardWithCard.cardInDeck.cardInDeck.easyCount,
                mediumCount = studyCardWithCard.cardInDeck.cardInDeck.mediumCount,
                hardCount = studyCardWithCard.cardInDeck.cardInDeck.hardCount,
                isLearned = studyCardWithCard.studyCardOfTheDay.learned
            )
        }

        // Sort: unfinished cards first, then alphabetically by front
        val sortedCards = cards.sortedWith(
            compareBy<StudyDeckCardView> { it.isLearned }
                .thenBy { it.front }
        )

        println("[HomePageViewModel] getStudyDeckCards() -> returning ${sortedCards.size} cards")
        return sortedCards
    }

}
