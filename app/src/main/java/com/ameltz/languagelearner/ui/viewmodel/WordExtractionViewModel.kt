package com.ameltz.languagelearner.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ameltz.languagelearner.data.api.AnthropicApiService
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.model.ExtractedWordPair
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.Uuid

sealed class WordExtractionState {
    object Idle : WordExtractionState()
    object Loading : WordExtractionState()
    data class Success(val wordPairs: List<ExtractedWordPair>, val imageUri: Uri) : WordExtractionState()
    data class Error(val message: String) : WordExtractionState()
}

@HiltViewModel
class WordExtractionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: Repository
) : ViewModel() {

    private val anthropicService = AnthropicApiService(context)

    private val _state = MutableStateFlow<WordExtractionState>(WordExtractionState.Idle)
    val state: StateFlow<WordExtractionState> = _state.asStateFlow()

    fun extractWordsFromImage(imageUri: Uri, apiKey: String) {
        viewModelScope.launch {
            _state.value = WordExtractionState.Loading

            anthropicService.extractWordsFromImage(imageUri, apiKey)
                .onSuccess { wordPairs ->
                    if (wordPairs.isEmpty()) {
                        _state.value = WordExtractionState.Error("No words found in image")
                    } else {
                        _state.value = WordExtractionState.Success(wordPairs, imageUri)
                    }
                }
                .onFailure { error ->
                    _state.value = WordExtractionState.Error(
                        error.message ?: "Failed to extract words"
                    )
                }
        }
    }

    fun importWordPairs(
        wordPairs: List<ExtractedWordPair>,
        japaneseToEnglishDeckId: Uuid?,
        englishToJapaneseDeckId: Uuid?
    ) {
        // Import Japanese -> English cards
        if (japaneseToEnglishDeckId != null) {
            val deck = repository.getDeck(japaneseToEnglishDeckId)
            if (deck != null) {
                wordPairs.forEach { wordPair ->
                    var card = Card.createCard(wordPair.japanese, wordPair.english)
                    try {
                        repository.upsertCard(card)
                        card = repository.getCard(card.front, card.back)!!
                        val deckCard = CardInDeck.createCardInDeck(card.uuid, deck.deck.uuid)
                        repository.upsertAllCardInDecks(listOf(deckCard), false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Import English -> Japanese cards
        if (englishToJapaneseDeckId != null) {
            val deck = repository.getDeck(englishToJapaneseDeckId)
            if (deck != null) {
                wordPairs.forEach { wordPair ->
                    var card = Card.createCard(wordPair.english, wordPair.japanese)
                    try {
                        repository.upsertCard(card)
                        card = repository.getCard(card.front, card.back)!!
                        val deckCard = CardInDeck.createCardInDeck(card.uuid, deck.deck.uuid)
                        repository.upsertAllCardInDecks(listOf(deckCard), false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun resetState() {
        _state.value = WordExtractionState.Idle
    }
}
