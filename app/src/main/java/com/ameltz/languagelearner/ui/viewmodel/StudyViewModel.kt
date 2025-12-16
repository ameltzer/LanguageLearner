package com.ameltz.languagelearner.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.StudyCardWithCard
import com.ameltz.languagelearner.data.entity.StudyDeckWithCards
import com.ameltz.languagelearner.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlin.uuid.Uuid

@HiltViewModel
class StudyViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    var currentCardIndex by mutableIntStateOf(0)
        private set

    var isFlipped by mutableStateOf(false)
        private set

    var currentDeckId by mutableStateOf<Uuid?>(null)
        private set


    fun loadStudyDeck(studyDeckId: Uuid): StudyDeckWithCards {
        println("[StudyViewModel] loadStudyDeck() called with studyDeckId: $studyDeckId")
        val studyDeck = repository.getStudyDeck(studyDeckId)
        if (studyDeck != null) {
            currentCardIndex = 0
            isFlipped = false
            currentDeckId = studyDeckId
            println("[StudyViewModel] loadStudyDeck() -> loaded ${studyDeck.cards.size} cards, reset index to 0")
        }
        return studyDeck!!
    }

    fun flipCard() {
        println("[StudyViewModel] flipCard() called, isFlipped: $isFlipped -> ${!isFlipped}")
        isFlipped = !isFlipped
    }

    fun nextCard() {
        println("[StudyViewModel] nextCard() called, currentCardIndex: $currentCardIndex -> ${currentCardIndex + 1}")
        currentCardIndex++
        isFlipped = false
    }

    fun isDone(studyDeckId: Uuid): Boolean {
        println("[StudyViewModel] isDone() called with studyDeckId: $studyDeckId")
        val result = repository.isDeckDone(studyDeckId)
        println("[StudyViewModel] isDone() -> $result")
        return result
    }

    fun updateCurrentCard(currentCard: StudyCardWithCard, difficulty: CardDifficulty) {
        println("[StudyViewModel] updateCurrentCard() called with difficulty: $difficulty for card ${currentCard.studyCardOfTheDay.uuid}")
        when (difficulty) {
            CardDifficulty.EASY -> {
                println("[StudyViewModel] updateCurrentCard() -> marking card as learned, incrementing easy count")
                currentCard.studyCardOfTheDay.learned = true
                repository.updateCardWithEasy(currentCard.studyCardOfTheDay.uuid)
            }
            CardDifficulty.MEDIUM -> {
                val delay = repository.getMediumTimeDelay()
                println("[StudyViewModel] updateCurrentCard() -> setting medium delay: $delay minutes, incrementing medium count")
                currentCard.studyCardOfTheDay.nextShowMinutes = delay
                repository.updateCardWithMedium(currentCard.studyCardOfTheDay.uuid)
            }
            else -> {
                val delay = repository.getHardTimeDelay()
                println("[StudyViewModel] updateCurrentCard() -> setting hard delay: $delay minutes, incrementing hard count")
                currentCard.studyCardOfTheDay.nextShowMinutes = delay
                repository.updateCardWithHard(currentCard.studyCardOfTheDay.uuid)
            }
        }
        currentCard.studyCardOfTheDay.lastAttempt = Instant.now().toEpochMilli()
        repository.upsertStudyCard(currentCard,  currentDeckId!!)
        println("[StudyViewModel] updateCurrentCard() -> completed")
    }
}

enum class CardDifficulty {
    EASY, MEDIUM, HARD
}
