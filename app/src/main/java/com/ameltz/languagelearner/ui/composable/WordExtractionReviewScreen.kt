package com.ameltz.languagelearner.ui.composable

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ameltz.languagelearner.ui.model.ExtractedWordPair
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import com.ameltz.languagelearner.ui.viewmodel.WordExtractionState
import com.ameltz.languagelearner.ui.viewmodel.WordExtractionViewModel
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordExtractionReviewScreen(
    wordExtractionViewModel: WordExtractionViewModel,
    homePageViewModel: HomePageViewModel,
    apiKey: String,
    onNavigateBack: () -> Unit
) {
    val state by wordExtractionViewModel.state.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            wordExtractionViewModel.extractWordsFromImage(it, apiKey)
        }
    }

    LaunchedEffect(Unit) {
        if (apiKey.isBlank()) {
            wordExtractionViewModel.resetState()
        } else {
            photoPickerLauncher.launch("image/*")
        }
    }

    LanguageLearnerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Extract Japanese Words") },
                    navigationIcon = {
                        IconButton(onClick = {
                            wordExtractionViewModel.resetState()
                            onNavigateBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (val currentState = state) {
                    is WordExtractionState.Idle -> {
                        if (apiKey.isBlank()) {
                            CenteredMessage("Please configure your Anthropic API key in Settings first")
                        } else {
                            CenteredMessage("Select an image to extract words")
                        }
                    }
                    is WordExtractionState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Analyzing image with Claude...")
                            }
                        }
                    }
                    is WordExtractionState.Success -> {
                        WordPairReviewContent(
                            wordPairs = currentState.wordPairs,
                            imageUri = currentState.imageUri,
                            homePageViewModel = homePageViewModel,
                            wordExtractionViewModel = wordExtractionViewModel,
                            onNavigateBack = onNavigateBack
                        )
                    }
                    is WordExtractionState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Error: ${currentState.message}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { photoPickerLauncher.launch("image/*") }) {
                                Text("Try Another Image")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordPairReviewContent(
    wordPairs: List<ExtractedWordPair>,
    imageUri: Uri,
    homePageViewModel: HomePageViewModel,
    wordExtractionViewModel: WordExtractionViewModel,
    onNavigateBack: () -> Unit
) {
    var editableWordPairs by remember { mutableStateOf(wordPairs) }
    var showDeckSelectionDialog by remember { mutableStateOf(false) }

    val allDecks = remember { homePageViewModel.getAllDeckSummaries { } }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Captured image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = "Extracted ${editableWordPairs.size} word pairs",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(editableWordPairs, key = { it.id }) { wordPair ->
                WordPairCard(
                    wordPair = wordPair,
                    onDelete = {
                        editableWordPairs = editableWordPairs.filter { it.id != wordPair.id }
                    },
                    onEdit = { newJapanese, newEnglish ->
                        editableWordPairs = editableWordPairs.map {
                            if (it.id == wordPair.id) {
                                it.copy(japanese = newJapanese, english = newEnglish)
                            } else it
                        }
                    }
                )
            }
        }

        Button(
            onClick = { showDeckSelectionDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = editableWordPairs.isNotEmpty()
        ) {
            Text("Import ${editableWordPairs.size} Word Pairs")
        }
    }

    if (showDeckSelectionDialog) {
        DeckSelectionDialog(
            allDecks = allDecks,
            onDismiss = { showDeckSelectionDialog = false },
            onConfirm = { japToEng, engToJap ->
                wordExtractionViewModel.importWordPairs(
                    editableWordPairs,
                    japToEng,
                    engToJap
                )
                showDeckSelectionDialog = false
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun WordPairCard(
    wordPair: ExtractedWordPair,
    onDelete: () -> Unit,
    onEdit: (String, String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wordPair.japanese,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = wordPair.english,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }

    if (showEditDialog) {
        EditWordPairDialog(
            wordPair = wordPair,
            onDismiss = { showEditDialog = false },
            onConfirm = { japanese, english ->
                onEdit(japanese, english)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun EditWordPairDialog(
    wordPair: ExtractedWordPair,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var japanese by remember { mutableStateOf(wordPair.japanese) }
    var english by remember { mutableStateOf(wordPair.english) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Word Pair") },
        text = {
            Column {
                OutlinedTextField(
                    value = japanese,
                    onValueChange = { japanese = it },
                    label = { Text("Japanese") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it },
                    label = { Text("English") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(japanese, english) },
                enabled = japanese.isNotBlank() && english.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckSelectionDialog(
    allDecks: List<com.ameltz.languagelearner.ui.model.HomePageDeckModel>,
    onDismiss: () -> Unit,
    onConfirm: (Uuid?, Uuid?) -> Unit
) {
    var japToEngDeckId by remember { mutableStateOf<Uuid?>(null) }
    var engToJapDeckId by remember { mutableStateOf<Uuid?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Decks for Import") },
        text = {
            Column {
                Text(
                    text = "Choose which decks to import cards into:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Japanese → English",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DeckDropdown(
                    allDecks = allDecks,
                    selectedDeckId = japToEngDeckId,
                    onDeckSelected = { japToEngDeckId = it },
                    label = "Select deck (optional)"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "English → Japanese",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DeckDropdown(
                    allDecks = allDecks,
                    selectedDeckId = engToJapDeckId,
                    onDeckSelected = { engToJapDeckId = it },
                    label = "Select deck (optional)"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(japToEngDeckId, engToJapDeckId) },
                enabled = japToEngDeckId != null || engToJapDeckId != null
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckDropdown(
    allDecks: List<com.ameltz.languagelearner.ui.model.HomePageDeckModel>,
    selectedDeckId: Uuid?,
    onDeckSelected: (Uuid?) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDeck = allDecks.find { it.deckId == selectedDeckId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedDeck?.deckName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onDeckSelected(null)
                    expanded = false
                }
            )
            allDecks.forEach { deck ->
                DropdownMenuItem(
                    text = { Text(deck.deckName) },
                    onClick = {
                        onDeckSelected(deck.deckId)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
