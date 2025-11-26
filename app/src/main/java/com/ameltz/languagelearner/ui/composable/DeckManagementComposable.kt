package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ameltz.languagelearner.data.entity.CardInDeckWithCard
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.DeckManagementViewModel
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
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
    var deckName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(deck?.deck?.name ?: ""))
    }
    var sortOption by remember { mutableStateOf(SortOption.NONE) }

    val sortedCards = deck?.cardsInDeck?.let { cards ->
        when (sortOption) {
            SortOption.BY_FRONT -> cards.sortedBy { it.card.front }
            SortOption.BY_BACK -> cards.sortedBy { it.card.back }
            SortOption.NONE -> cards
        }
    }

    LanguageLearnerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Manage Deck",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = toHomePage) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Deck name input
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = deckName,
                        onValueChange = { deckName = it },
                        label = { Text("Deck Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (deck != null) {
                                deckManagementViewModel.saveDeck(
                                    Deck(
                                        deck.deck.uuid,
                                        deckName.text,
                                        deck.deck.deckSettingsId
                                    )
                                )
                            }
                            toHomePage()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = deckName.text.isNotBlank()
                    ) {
                        Text("Save Deck")
                    }

                    Button(
                        onClick = {
                            if (deck != null) {
                                toCardCreation(deck.deck.uuid)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Associate Cards")
                    }

                    OutlinedButton(
                        onClick = {
                            if (deck != null) {
                                deckManagementViewModel.deleteDeck(deck)
                            }
                            toHomePage()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Delete Deck")
                    }
                }

                // Cards section header
                Text(
                    text = "Cards in Deck (${sortedCards?.size ?: 0})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Sort options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = sortOption == SortOption.NONE,
                        onClick = { sortOption = SortOption.NONE },
                        label = { Text("Default") }
                    )
                    FilterChip(
                        selected = sortOption == SortOption.BY_FRONT,
                        onClick = { sortOption = SortOption.BY_FRONT },
                        label = { Text("Sort by Front") }
                    )
                    FilterChip(
                        selected = sortOption == SortOption.BY_BACK,
                        onClick = { sortOption = SortOption.BY_BACK },
                        label = { Text("Sort by Back") }
                    )
                }

                // Cards list
                if (sortedCards.isNullOrEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No cards in this deck",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap 'Associate Cards' to add cards",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedCards) { cardInDeck ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { },
                                        onLongClick = { contextCardInDeck.value = cardInDeck }
                                    ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = cardInDeck.card.front,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cardInDeck.card.back,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = contextCardInDeck.value == cardInDeck,
                                onDismissRequest = { contextCardInDeck.value = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove from deck") },
                                    onClick = {
                                        deckManagementViewModel.deleteCardInDeck(cardInDeck)
                                        contextCardInDeck.value = null
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}