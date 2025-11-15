package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.Uuid

@HiltViewModel
class HomePageViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    fun getAllDeckSummaries(toManageDeck: (deckId: Uuid) -> Unit): List<HomePageDeckModel> {
        val studyMaterial = this.repository.getAllDecks().map { dbDeck ->
            dbDeck.generateStudyMaterial(
                {toManageDeck(dbDeck.deck.uuid)},
                50,
                repository
            )
        }



        return studyMaterial.map { it.first }
    }

}