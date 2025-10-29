package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.ui.viewmodel.AddCardViewModel
import com.ameltz.languagelearner.ui.viewmodel.DeckManagementViewModel
import kotlin.uuid.Uuid

data class CheckboxItem (val label: String, var isSelected: Boolean, val deckId: Uuid)


@Composable
fun AddCardComposable(addCardViewModel: AddCardViewModel,
                      back: () -> Unit,
                      deckId: Uuid? = null,
                      cardId: Uuid? = null) {
    val decks = addCardViewModel.getAllDecks().map { deck -> deck.deck }

    val card = addCardViewModel.getCard(cardId)

    var front by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(card?.card?.front ?: ""))
    }

    var back by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(card?.card?.back ?: ""))
    }

    val decksSelected = remember {
        mutableStateListOf(
            *decks.map { deck ->  CheckboxItem(deck.name,
                isSelected = (deckId != null && deckId == deck.uuid) || (card?.instancesOfCard?.any { cardInDeck -> cardInDeck.deckId == deck.uuid } ?: false),
                deck.uuid) }.toTypedArray()
        )
    }

    LanguageLearnerTheme {
        Column {
            TextField(
                value = front,
                onValueChange = {front = it},
                label = { Text("Front of card")}
            )
            TextField(
                value = back,
                onValueChange = {back = it},
                label = { Text("BAck of card")}
            )
            DropdownMenu(
                expanded = true,
                onDismissRequest = {}
            ) {
                decksSelected.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = {
                            Row {
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { isChecked ->
                                        decksSelected[index] = item.copy(isSelected = isChecked)
                                    }
                                )
                                Text(text = item.label)
                            }
                        },
                        onClick = {}
                    )
                }
            }
            Button(onClick = {
                val card = Card(Uuid.random(), front.text, back.text)
                val cardInDecks = decks.map { deck -> CardInDeck(Uuid.random(), 0, 0, card.uuid, deck.uuid) }
                addCardViewModel.addCard(cardInDecks, card)
                back()
            }) {
                Text(text = "Add Card to Decks")
            }
        }
    }
}