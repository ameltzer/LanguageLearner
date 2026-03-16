package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ameltz.languagelearner.data.api.VoiceCardAnalysisService
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.model.VoiceCardData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.Uuid

sealed class VoiceCardState {
    object Idle : VoiceCardState()
    object Listening : VoiceCardState()
    object Processing : VoiceCardState()
    data class ConfirmationNeeded(val cardData: VoiceCardData, val transcript: String) : VoiceCardState()
    data class Success(val message: String) : VoiceCardState()
    data class Error(val message: String) : VoiceCardState()
}

@HiltViewModel
class VoiceCardCreationViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val analysisService = VoiceCardAnalysisService()

    private val _state = MutableStateFlow<VoiceCardState>(VoiceCardState.Idle)
    val state: StateFlow<VoiceCardState> = _state.asStateFlow()

    fun startListening() {
        _state.value = VoiceCardState.Listening
    }

    fun processVoiceTranscript(transcript: String, apiKey: String) {
        viewModelScope.launch {
            _state.value = VoiceCardState.Processing

            analysisService.analyzeVoiceCommand(transcript, apiKey)
                .onSuccess { cardData ->
                    // Fuzzy match deck name
                    val matchedDeck = findBestMatchingDeck(cardData.deckName)
                    val updatedCardData = cardData.copy(matchedDeckId = matchedDeck?.deck?.uuid)

                    _state.value = VoiceCardState.ConfirmationNeeded(updatedCardData, transcript)
                }
                .onFailure { error ->
                    _state.value = VoiceCardState.Error(
                        error.message ?: "Failed to analyze voice command"
                    )
                }
        }
    }

    fun createCard(cardData: VoiceCardData) {
        viewModelScope.launch {
            try {
                if (cardData.matchedDeckId == null) {
                    _state.value = VoiceCardState.Error("No deck selected")
                    return@launch
                }

                val card = Card.createCard(cardData.front, cardData.back)
                repository.upsertCard(card)

                val savedCard = repository.getCard(card.front, card.back)
                    ?: throw Exception("Failed to save card")

                val cardInDeck = CardInDeck.createCardInDeck(savedCard.uuid, cardData.matchedDeckId)
                repository.upsertAllCardInDecks(listOf(cardInDeck), false)

                _state.value = VoiceCardState.Success("Card added successfully!")
            } catch (e: Exception) {
                _state.value = VoiceCardState.Error(
                    "Failed to create card: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        _state.value = VoiceCardState.Idle
    }

    private fun findBestMatchingDeck(deckName: String): com.ameltz.languagelearner.data.entity.CardInDeckAndDeckRelation? {
        val allDecks = repository.getAllDecks()
        if (allDecks.isEmpty()) return null

        val normalizedInput = deckName.lowercase().trim()

        // Try exact match first
        val exactMatch = allDecks.find {
            it.deck.name.lowercase() == normalizedInput
        }
        if (exactMatch != null) return exactMatch

        // Try contains match
        val containsMatch = allDecks.find {
            it.deck.name.lowercase().contains(normalizedInput) ||
            normalizedInput.contains(it.deck.name.lowercase())
        }
        if (containsMatch != null) return containsMatch

        // Calculate Levenshtein distance for fuzzy matching
        val scoredDecks = allDecks.map { deck ->
            val distance = levenshteinDistance(
                normalizedInput,
                deck.deck.name.lowercase()
            )
            val similarity = 1.0 - (distance.toDouble() / maxOf(normalizedInput.length, deck.deck.name.length))
            deck to similarity
        }

        // Return best match if similarity is at least 50%
        val bestMatch = scoredDecks.maxByOrNull { it.second }
        return if (bestMatch != null && bestMatch.second >= 0.5) {
            bestMatch.first
        } else {
            // Default to first deck if no good match
            allDecks.firstOrNull()
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val costs = IntArray(s2.length + 1)
        for (j in costs.indices) costs[j] = j

        for (i in 1..s1.length) {
            var lastValue = i
            for (j in 1..s2.length) {
                if (s1[i - 1] == s2[j - 1]) {
                    costs[j] = costs[j - 1]
                } else {
                    val newValue = minOf(
                        costs[j - 1],  // insertion
                        lastValue,     // deletion
                        costs[j]       // substitution
                    ) + 1
                    costs[j] = newValue
                }
                val temp = costs[j]
                costs[j] = lastValue
                lastValue = temp
            }
            costs[0] = i
        }
        return costs[s2.length]
    }
}
