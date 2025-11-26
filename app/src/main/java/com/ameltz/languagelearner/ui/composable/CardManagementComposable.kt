package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.TextFieldValue
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.ui.viewmodel.AddCardViewModel
import kotlin.uuid.Uuid

data class CheckboxItem (val label: String, var isSelected: Boolean, val deckId: Uuid)

enum class DeckSortOption {
    NONE,
    BY_NAME
}

@Composable
fun CardManagementComposable(addCardViewModel: AddCardViewModel,
                             back: () -> Unit,
                             cardId: Uuid? = null) {
    val decks = addCardViewModel.getAllDecks().map { deck -> deck.deck }

    val card = addCardViewModel.getCard(cardId)

    var front by rememberSaveable(cardId, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(card?.card?.front ?: ""))
    }

    var back by rememberSaveable(cardId, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(card?.card?.back ?: ""))
    }

    var sortOption by remember { mutableStateOf(DeckSortOption.NONE) }

    val decksSelected = remember {
        mutableStateListOf(
            *decks.map { deck ->  CheckboxItem(deck.name,
                isSelected = (card?.instancesOfCard?.any { cardInDeck -> cardInDeck.deckId == deck.uuid } ?: false),
                deck.uuid) }.toTypedArray()
        )
    }

    val sortedDecks = when (sortOption) {
        DeckSortOption.BY_NAME -> decksSelected.sortedBy { it.label }
        DeckSortOption.NONE -> decksSelected
    }

    LanguageLearnerTheme {
        Column {
            Row {
                TextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Front of card") }
                )
                TextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("Back of card") }
                )
            }
            Row {
                Button(onClick = { sortOption = DeckSortOption.NONE }) {
                    Text("No Sort")
                }
                Button(onClick = { sortOption = DeckSortOption.BY_NAME }) {
                    Text("Sort by Name")
                }
            }
            Column(modifier = Modifier.verticalScroll(rememberScrollState()))  {
                sortedDecks.forEach { item ->
                    Row {
                        Text(text = item.label)
                        Checkbox(
                            checked = item.isSelected,
                            onCheckedChange = { isChecked ->
                                val originalIndex = decksSelected.indexOfFirst { it.deckId == item.deckId }
                                if (originalIndex != -1) {
                                    decksSelected[originalIndex] = item.copy(isSelected = isChecked)
                                }
                            }
                        )
                    }
                }
            }

            Button(onClick = {
                val decksSelectedIds = decksSelected.filter { it.isSelected }.map { it.deckId }
                val decksNotSelected = decksSelected.filter { !it.isSelected }.map { it.deckId }
                if (!decksSelectedIds.isEmpty()) {
                    val selectedDecks = decks.filter { decksSelectedIds.contains(it.uuid) }
                    val card = Card(card?.card?.uuid ?: Uuid.random(), front.text, back.text)
                    val cardInDecks =
                        selectedDecks.map { deck ->
                            CardInDeck.createCardInDeck(
                                card.uuid,
                                deck.uuid
                            )
                        }
                    addCardViewModel.addCard(cardInDecks, card)
                }
                if (!decksNotSelected.isEmpty() && cardId != null) {
                    addCardViewModel.deleteCardInDecks(cardId, decksNotSelected)
                }
                back()
            }) {
                Text(text = "Save")
            }
            Button(onClick = {
                if (cardId != null) {
                    addCardViewModel.deleteCard(cardId)
                    back()
                }
            }) {
                Text(text = "Delete")
            }
        }
    }
}