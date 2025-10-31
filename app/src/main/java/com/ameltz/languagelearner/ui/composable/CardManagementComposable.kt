package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.ui.viewmodel.AddCardViewModel
import kotlin.uuid.Uuid

data class CheckboxItem (val label: String, var isSelected: Boolean, val deckId: Uuid)


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

    val decksSelected = remember {
        mutableStateListOf(
            *decks.map { deck ->  CheckboxItem(deck.name,
                isSelected = (card?.instancesOfCard?.any { cardInDeck -> cardInDeck.deckId == deck.uuid } ?: false),
                deck.uuid) }.toTypedArray()
        )
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
            Column {
                decksSelected.forEachIndexed { index, item ->
                    Row {
                        Text(text = item.label)
                        Checkbox(
                            checked = item.isSelected,
                            onCheckedChange = { isChecked ->
                                decksSelected[index] = item.copy(isSelected = isChecked)
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
                            CardInDeck(
                                Uuid.random(),
                                0,
                                0,
                                card.uuid,
                                deck.uuid
                            )
                        }
                    addCardViewModel.addCard(cardInDecks, card)
                    back()
                }
                if (!decksNotSelected.isEmpty() && cardId != null) {
                    decksNotSelected.forEach {
                        addCardViewModel.deleteCardInDeck(cardId, it)
                    }
                }
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