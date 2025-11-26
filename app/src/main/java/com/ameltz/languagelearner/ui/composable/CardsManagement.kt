package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.CardsManagementViewModel
import kotlin.uuid.Uuid

enum class SortOption {
    NONE,
    BY_FRONT,
    BY_BACK
}

@Composable
fun CardsManagement(cardsManagementViewModel: CardsManagementViewModel, back: () -> Unit,
                    toCard: (cardId: Uuid) -> Unit, toAddCard: () -> Unit) {

    var sortOption by remember { mutableStateOf(SortOption.NONE) }
    val allCards = cardsManagementViewModel.getAllCards()

    val cards = when (sortOption) {
        SortOption.BY_FRONT -> allCards.sortedBy { it.front }
        SortOption.BY_BACK -> allCards.sortedBy { it.back }
        SortOption.NONE -> allCards
    }

    LanguageLearnerTheme {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row {
                Button(onClick = { toAddCard() }) {
                    Text("Create Card")
                }
                Button(onClick = { back() }) {
                    Text("Back")
                }
            }
            Row {
                Button(onClick = { sortOption = SortOption.NONE }) {
                    Text("No Sort")
                }
                Button(onClick = { sortOption = SortOption.BY_FRONT }) {
                    Text("Sort by Front")
                }
                Button(onClick = { sortOption = SortOption.BY_BACK }) {
                    Text("Sort by Back")
                }
            }
            cards.forEach { card ->
                Row(modifier = Modifier.clickable(onClick = { toCard(card.uuid) })) {
                    Text(card.display())
                }
            }
        }
    }

}