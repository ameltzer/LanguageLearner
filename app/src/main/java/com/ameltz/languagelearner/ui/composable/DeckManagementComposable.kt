package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.ameltz.languagelearner.data.entity.CardInDeckWithCard
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.DeckManagementViewModel
import kotlin.uuid.Uuid

@Composable
fun DeckManagement(
    deckManagementViewModel: DeckManagementViewModel,
    deckId: Uuid,
    toHomePage: () -> Unit,
    toCardCreation: (deckId: Uuid) -> Unit
) {
    val deck = deckManagementViewModel.getDeck(deckId)
    val contextCardInDeck: MutableState<CardInDeckWithCard?> = rememberSaveable {
        mutableStateOf(null)
    }
    var front by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(deck?.deck?.name ?: ""))
    }
    LanguageLearnerTheme {
        Column {
            Row {
                Button(onClick = {
                    toHomePage()
                }) {
                    Text(text = "Go back")
                }
                Button(onClick = {
                    if (deck != null) {
                        deckManagementViewModel.saveDeck(
                            Deck(
                                deck.deck.uuid,
                                front.text,
                                deck.deck.deckSettingsId
                            )
                        )
                    }
                }) {
                    Text(text = "Save")
                }
            }
            Row {
                TextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Deck name") }
                )
                Button(onClick = {
                    if (deck != null) {
                        deckManagementViewModel.deleteDeck(deck)
                    }
                    toHomePage()
                }) {
                    Text(text = (if (deck != null) "Delete this deck" else "Go back to home page"))

                }
            }
            Button(onClick = {
                if (deck != null) {
                    toCardCreation(deck.deck.uuid)
                }
            }) {
                Text(text = "Associate Cards")
            }
            (deck?.cardsInDeck?.forEach { cardInDeck ->
                Text(
                    text = cardInDeck.card.display(),
                    modifier = Modifier.combinedClickable(
                        onClick = { print(cardInDeck.card.display()) },
                        onLongClick = { contextCardInDeck.value = cardInDeck }
                    )
                )
                DropdownMenu(
                    expanded = contextCardInDeck.value == cardInDeck,
                    onDismissRequest = { contextCardInDeck.value = null }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete card") },
                        onClick = {
                            deckManagementViewModel.deleteCardInDeck(cardInDeck)
                            contextCardInDeck.value = null
                        }
                    )
                }
            })
        }
    }
}