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

    fun isDone(studyDeckId: Uuid): Boolean {
        return repository.isDeckDone(studyDeckId)
    }

    fun updateCurrentCard(difficulty: CardDifficulty) {
        when (difficulty) {
            CardDifficulty.EASY -> {
                currentCard?.learned = true
                repository.updateCardInDeckNextDay(currentCard!!.studyCardId)
            }
            CardDifficulty.MEDIUM -> {
                currentCard?.nextShowMins = repository.getMediumTimeDelay()
            }
            else -> {
                currentCard?.nextShowMins = repository.getHardTimeDelay()
            }
        }
        if (currentCard != null) {
            currentCard!!.lastAttempt = Instant.now()
            repository.upsertStudyCard(currentCard!!,  currentDeckId!!)
        }
    }
}

enum class CardDifficulty {
    EASY, MEDIUM, HARD
}
