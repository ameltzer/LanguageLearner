package com.ameltz.languagelearner.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.model.StudyCardOfTheDay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

@HiltViewModel
class StudyViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    var cards by mutableStateOf<List<StudyCardOfTheDay>>(emptyList())
        private set

    var currentCardIndex by mutableIntStateOf(0)
        private set

    var isFlipped by mutableStateOf(false)
        private set

    val currentCard: StudyCardOfTheDay?
        get() = cards.getOrNull(currentCardIndex)

    val isLastCard: Boolean
        get() = currentCardIndex >= cards.size - 1

    val hasCards: Boolean
        get() = cards.isNotEmpty()

    var currentDeckId by mutableStateOf<Uuid?>(null)
        private set


    fun loadStudyDeck(studyDeckId: Uuid) {
        val studyDeck = repository.getStudyDeck(studyDeckId, Instant.now().truncatedTo(ChronoUnit.DAYS))
        if (studyDeck != null) {
            cards = studyDeck.toStudyDeckOfTheDay().cards
            currentCardIndex = 0
            isFlipped = false
            currentDeckId = studyDeckId
        }
    }

    fun flipCard() {
        isFlipped = !isFlipped
    }

    fun nextCard() {
        if (currentCardIndex < cards.size - 1) {
            currentCardIndex++
            isFlipped = false
        }
    }

    fun resetStudySession() {
        currentCardIndex = 0
        isFlipped = false
    }

    fun updateCurrentCard(difficulty: CardDifficulty) {
        if (difficulty == CardDifficulty.EASY) {
            currentCard?.learned = true
            currentCard?.nextShowDays =
                (currentCard?.nextShowDays?.times(1.2)?.roundToInt() ?: 1).coerceAtLeast(1)
        } else if (difficulty == CardDifficulty.MEDIUM) {
            currentCard?.nextShowMins = 60
        } else {
            currentCard?.nextShowMins = 15
        }
        if (currentCard != null) {
            repository.upsertStudyCard(currentCard!!,  currentDeckId!!)
        }
    }
}

enum class CardDifficulty {
    EASY, MEDIUM, HARD
}
