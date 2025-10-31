package com.ameltz.languagelearner.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.ui.viewmodel.AddCardsToDeckViewModel
import kotlin.uuid.Uuid

@Composable
fun AddCardsToDeck(
    deckId: Uuid,
    addCardsToDeckViewModel: AddCardsToDeckViewModel,
    back: () -> Unit,
    modifier: Modifier = Modifier
) {
    var items by remember { mutableStateOf<List<Card>>(emptyList()) }
    var selectedItems by remember { mutableStateOf<Set<Card>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var displayName by remember { mutableStateOf("Deck Name") }


    // Mock query for list of strings
    LaunchedEffect(deckId) {
        val deck = addCardsToDeckViewModel.getDeck(deckId)
        items = addCardsToDeckViewModel.getAllCards()
        if (deck != null) {
            val cardInDeck = deck.cardsInDeck.map { it.card.uuid }
            items.forEach { card ->

                    if (cardInDeck.contains(card.uuid)) {
                        selectedItems = selectedItems + card
                    }

            }
        }
        isLoading = false
        displayName = deck?.deck?.name ?: ""
    }

    if (displayName == "") {
        Button(onClick = {back()}) {
            Text(text = "Go back")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top name display
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // List of selectable items
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column {
                Button(onClick = {back()}) {
                    Text(text = "Go back")
                }
                items.forEach { item ->
                    SelectableRow(
                        text = item.display(),
                        isSelected = selectedItems.contains(item),
                        onSelect = {
                            selectedItems = if (selectedItems.contains(item)) {
                                selectedItems - item
                            } else {
                                selectedItems + item
                            }
                        }
                    )
                }
            }
        }

        // Submit button at bottom
        Button(
            onClick = {
                addCardsToDeckViewModel.addCardsToDeck(deckId, selectedItems.map { it.uuid })
                val cardsToRemove = items.filter { !selectedItems.contains(it) }.map { it.uuid  }
                addCardsToDeckViewModel.removeCardsFromDeck(deckId, cardsToRemove)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun SelectableRow(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        color = backgroundColor,
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


