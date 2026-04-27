package com.ameltz.languagelearner.ui.composable

import android.database.sqlite.SQLiteConstraintException
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.entity.CardInDeck
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.AddCardViewModel
import kotlin.uuid.Uuid

data class CheckboxItem(val label: String, var isSelected: Boolean, val deckId: Uuid)

enum class DeckSortOption {
    NONE,
    BY_NAME
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardManagementComposable(
    addCardViewModel: AddCardViewModel,
    onBack: () -> Unit,
    cardId: Uuid? = null,
    deckId: Uuid? = null
) {
    val decks = addCardViewModel.getAllDecks().map { deck -> deck.deck }
    val card = addCardViewModel.getCard(cardId)

    var front by rememberSaveable(cardId, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(card?.card?.front ?: ""))
    }

    var back by rememberSaveable(cardId, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(card?.card?.back ?: ""))
    }

    val categorization = remember {
        mutableStateListOf(*(card?.card?.categorization ?: emptyList<String>()).toTypedArray())
    }

    var newCategory by remember { mutableStateOf("") }

    var sortOption by remember { mutableStateOf(DeckSortOption.NONE) }

    var showDuplicateCardDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val tts = remember { TextToSpeech(context) { } }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val decksSelected = remember {
        mutableStateListOf(
            *decks.map { deck ->
                CheckboxItem(
                    deck.name,
                    isSelected = if (cardId != null) {
                        // Editing existing card - select decks the card is already in
                        card?.instancesOfCard?.any { cardInDeck -> cardInDeck.deckId == deck.uuid } ?: false
                    } else if (deckId != null) {
                        // Creating new card from a specific deck - pre-select that deck
                        deck.uuid == deckId
                    } else {
                        // Creating new card without a specific deck - don't pre-select anything
                        false
                    },
                    deck.uuid
                )
            }.toTypedArray()
        )
    }

    val sortedDecks = when (sortOption) {
        DeckSortOption.BY_NAME -> decksSelected.sortedBy { it.label }
        DeckSortOption.NONE -> decksSelected
    }

    LanguageLearnerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (cardId == null) "Create Card" else "Edit Card",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {onBack()}) {
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
                // Card input fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = front,
                        onValueChange = { front = it },
                        label = { Text("Front of card") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2,
                        trailingIcon = {
                            IconButton(onClick = {
                                tts.language = detectLocale(front.text)
                                tts.speak(front.text, TextToSpeech.QUEUE_FLUSH, null, null)
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Speak front")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = back,
                        onValueChange = { back = it },
                        label = { Text("Back of card") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2,
                        trailingIcon = {
                            IconButton(onClick = {
                                tts.language = detectLocale(back.text)
                                tts.speak(back.text, TextToSpeech.QUEUE_FLUSH, null, null)
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Speak back")
                            }
                        }
                    )

                    // Categorization
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorization.forEachIndexed { index, category ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(category) },
                                trailingIcon = {
                                    IconButton(onClick = { categorization.removeAt(index) }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Remove $category"
                                        )
                                    }
                                }
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCategory,
                            onValueChange = { newCategory = it },
                            label = { Text("New category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                val trimmed = newCategory.trim()
                                if (trimmed.isNotEmpty() && !categorization.contains(trimmed)) {
                                    categorization.add(trimmed)
                                }
                                newCategory = ""
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add category")
                        }
                    }
                }

                // Deck selection header
                Text(
                    text = "Add to Decks",
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
                        selected = sortOption == DeckSortOption.NONE,
                        onClick = { sortOption = DeckSortOption.NONE },
                        label = { Text("Default") },
                        leadingIcon = if (sortOption == DeckSortOption.NONE) {
                            { Icon(Icons.Default.Clear, contentDescription = null) }
                        } else null
                    )
                    FilterChip(
                        selected = sortOption == DeckSortOption.BY_NAME,
                        onClick = { sortOption = DeckSortOption.BY_NAME },
                        label = { Text("Sort by Name") }
                    )
                }

                // Deck list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedDecks) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { isChecked ->
                                        val originalIndex =
                                            decksSelected.indexOfFirst { it.deckId == item.deckId }
                                        if (originalIndex != -1) {
                                            decksSelected[originalIndex] =
                                                item.copy(isSelected = isChecked)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val decksSelectedIds = decksSelected.filter { it.isSelected }.map { it.deckId }
                            val decksNotSelected = decksSelected.filter { !it.isSelected }.map { it.deckId }
                            if (!decksSelectedIds.isEmpty()) {
                                val selectedDecks = decks.filter { decksSelectedIds.contains(it.uuid) }
                                val card = Card(card?.card?.uuid ?: Uuid.random(), front.text, back.text, categorization.toList())
                                val cardInDecks =
                                    selectedDecks.map { deck ->
                                        CardInDeck.createCardInDeck(
                                            card.uuid,
                                            deck.uuid
                                        )
                                    }
                                try {
                                    addCardViewModel.addCard(cardInDecks, card)
                                } catch (ex: SQLiteConstraintException) {
                                    showDuplicateCardDialog = true
                                    return@Button
                                }
                            }
                            if (!decksNotSelected.isEmpty() && cardId != null) {
                                addCardViewModel.deleteCardInDecks(cardId, decksNotSelected)
                            }
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = front.text.isNotBlank() && back.text.isNotBlank()
                    ) {
                        Text(text = "Save Card")
                    }

                    if (cardId != null) {
                        OutlinedButton(
                            onClick = {
                                addCardViewModel.deleteCard(cardId)
                                onBack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(text = "Delete Card")
                        }
                    }
                }
            }
        }

        // Duplicate card warning dialog
        if (showDuplicateCardDialog) {
            AlertDialog(
                onDismissRequest = { showDuplicateCardDialog = false },
                title = { Text("Duplicate Card") },
                text = { Text("A card with this front and back already exists. Please modify the card content to make it unique.") },
                confirmButton = {
                    TextButton(onClick = { showDuplicateCardDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}