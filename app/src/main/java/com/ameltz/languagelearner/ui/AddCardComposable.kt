package com.ameltz.languagelearner.ui

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
import java.util.UUID

data class CheckboxItem(val label: String, var isSelected: Boolean, val deckId: UUID)


@Composable
fun AddCard(decks: List<Deck>, addCardViewModel: AddCardViewModel) {
    var front by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    var back by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    val decksSelected = remember {
        mutableStateListOf(
            *decks.map { deck ->  CheckboxItem(deck.name, isSelected = false, deck.uuid) }.toTypedArray()
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
                val card = Card(UUID.randomUUID(), front.text, back.text)
                val cardInDecks = decks.map { deck -> CardInDeck(UUID.randomUUID(), 0, 0, card.uuid, deck.uuid) }
                addCardViewModel.addCard(cardInDecks, card)
            }) {
                Text(text = "Add Card to Decks")
            }
        }
    }
}