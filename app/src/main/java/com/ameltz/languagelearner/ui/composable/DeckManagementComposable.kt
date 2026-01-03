package com.ameltz.languagelearner.ui.composable

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
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
    toCardCreation: (deckId: Uuid) -> Unit,
    toCard: (cardId: Uuid) -> Unit
) {
    val deck = deckManagementViewModel.getDeck(deckId)
    val contextCardInDeck: MutableState<CardInDeckWithCard?> = rememberSaveable {
        mutableStateOf(null)
    }
    var deckName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(deck?.deck?.name ?: ""))
    }
    var sortOption by remember { mutableStateOf(SortOption.NONE) }
    var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.CreateDocument("text/tab-separated-values") {
            override fun createIntent(context: android.content.Context, input: String): Intent {
                val intent = super.createIntent(context, input)

                // Try to set initial URI to Google Drive
                // This will suggest Google Drive as the location, and the user can navigate to the Japanese folder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        // Google Drive provider authority
                        val driveAuthority = "com.google.android.apps.docs.storage"

                        // Build a generic Google Drive URI
                        // The user can navigate to their Japanese folder from here
                        val driveRootUri = Uri.parse("content://$driveAuthority/document/acc%3D1%3Bdoc%3Droot")

                        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, driveRootUri)
                    } catch (e: Exception) {
                        // If Google Drive is not available, the system will use default location
                        e.printStackTrace()
                    }
                }

                return intent
            }
        }
    ) { uri: Uri? ->
        uri?.let {
            try {
                if (deck != null) {
                    val exportContent = deckManagementViewModel.exportDeckToTSV(deck)
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(exportContent.toByteArray())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val sortedCards = deck?.cardsInDeck?.let { cards ->
        val sorted = when (sortOption) {
            SortOption.BY_FRONT -> cards.sortedBy { it.card.front.lowercase() }
            SortOption.BY_BACK -> cards.sortedBy { it.card.back.lowercase() }
            SortOption.BY_ENGLISH -> cards.sortedBy { cardInDeck ->
                val card = cardInDeck.card
                val latinCharCountFront = card.front.count { it.code in 0..127 }
                val englishText = if (latinCharCountFront > card.front.length / 2) card.front else card.back
                englishText.lowercase()
            }
            SortOption.NONE -> cards
        }

        if (searchQuery.text.isNotBlank()) {
            sorted.filter { cardInDeck ->
                cardInDeck.card.front.contains(searchQuery.text, ignoreCase = true) ||
                cardInDeck.card.back.contains(searchQuery.text, ignoreCase = true)
            }
        } else {
            sorted
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
                // Deck name and actions - compact layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deckName,
                        onValueChange = { deckName = it },
                        label = { Text("Deck Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Action buttons in compact rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                            modifier = Modifier.weight(1f),
                            enabled = deckName.text.isNotBlank()
                        ) {
                            Text("Save")
                        }

                        Button(
                            onClick = {
                                if (deck != null) {
                                    toCardCreation(deck.deck.uuid)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text("Cards")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (deck != null) {
                                    exportLauncher.launch("${deckName.text}.tsv")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = deck != null && deck.cardsInDeck.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text("Export")
                        }

                        OutlinedButton(
                            onClick = {
                                if (deck != null) {
                                    deckManagementViewModel.deleteDeck(deck)
                                }
                                toHomePage()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text("Delete")
                        }
                    }
                }

                // Cards section header and controls - compact
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Cards in Deck (${sortedCards?.size ?: 0})",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Sort options - compact
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = sortOption == SortOption.NONE,
                            onClick = { sortOption = SortOption.NONE },
                            label = { Text("Default", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = sortOption == SortOption.BY_FRONT,
                            onClick = { sortOption = SortOption.BY_FRONT },
                            label = { Text("Front", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = sortOption == SortOption.BY_BACK,
                            onClick = { sortOption = SortOption.BY_BACK },
                            label = { Text("Back", style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    // Search bar - compact
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search", style = MaterialTheme.typography.labelMedium) },
                        placeholder = { Text("Search cards...", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (searchQuery.text.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true
                    )
                }

                // Cards list
                if (sortedCards.isNullOrEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No cards in this deck",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tap 'Cards' to add cards",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sortedCards) { cardInDeck ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { toCard(cardInDeck.card.uuid) },
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