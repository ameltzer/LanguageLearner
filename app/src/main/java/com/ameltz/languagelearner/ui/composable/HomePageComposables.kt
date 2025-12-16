package com.ameltz.languagelearner.ui.composable

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ameltz.languagelearner.ui.model.AnkiCard
import com.ameltz.languagelearner.ui.model.AnkiDeckImport
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.BulkImportViewModel
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.collections.forEach
import kotlin.uuid.Uuid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    toNewDeck: () -> Unit,
    toManageDeck: (deckId: Uuid) -> Unit,
    homePageViewModel: HomePageViewModel,
    toCardManagement: () -> Unit,
    bulkImportViewModel: BulkImportViewModel,
    toStudyDeck: (studyDeckId: Uuid) -> Unit,
    toSettings: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var decks by remember { mutableStateOf(homePageViewModel.getAllDeckSummaries(toManageDeck)) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            decks = homePageViewModel.getAllDeckSummaries(toManageDeck)
            delay(300)
            isRefreshing = false
        }
    }
    val state = rememberPullToRefreshState()

    LanguageLearnerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Language Learner",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    actions = {
                        IconButton(onClick = toSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = toNewDeck,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Deck") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = state,
                indicator = {
                    Indicator(isRefreshing = isRefreshing, state = state)
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    TopBanner(toCardManagement, bulkImportViewModel, onRefresh)
                    DeckDisplay(decks, toStudyDeck, homePageViewModel, onRefresh)
                }
            }
        }
    }
}

@Composable
fun TopBanner(
    toCardManagement: () -> Unit,
    bulkImportViewModel: BulkImportViewModel,
    onRefresh: () -> Unit
) {
    var fileContent by remember { mutableStateOf<String?>(null) }
    var deckName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        fileContent = reader.readText()
                    }
                }
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex =
                            cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            deckName = cursor.getString(nameIndex)
                        }
                    }
                }
                if (deckName == null) {
                    deckName = it.lastPathSegment ?: "imported_deck"
                }
                if (deckName != null) {
                    deckName = File(deckName).nameWithoutExtension
                }
                if (fileContent != null && deckName != null) {
                    val cards = fileContent!!.split("\n")
                        .filter { it.isNotBlank() }
                        .map { line ->
                            val parts = line.split("\t")
                            AnkiCard(parts[0], parts[1])
                        }
                    val ankiDeck = AnkiDeckImport(deckName!!, cards)
                    bulkImportViewModel.importAnkiDeck(ankiDeck)
                }
                onRefresh()
            } catch (e: Exception) {
                e.printStackTrace()
                deckName = null
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalButton(
            onClick = toCardManagement,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Cards")
        }
        FilledTonalButton(
            onClick = { filePicker.launch("*/*") },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.AddCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Import")
        }
    }
}

@Composable
fun DeckDisplay(
    decks: List<HomePageDeckModel>,
    toStudyDeck: (studyDeckId: Uuid) -> Unit,
    homePageViewModel: HomePageViewModel,
    onRefresh: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    if (decks.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                "No decks yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Create a deck to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(decks) { deck ->
                var showMenu by remember { mutableStateOf(false) }
                var showHardDeckMessage by remember { mutableStateOf<String?>(null) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { toStudyDeck(deck.todaysDeckId) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showMenu = true
                            }
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = deck.deckName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DeckStatBadge(
                                count = deck.newCardsDue,
                                label = "New",
                                color = MaterialTheme.colorScheme.primary
                            )
                            DeckStatBadge(
                                count = deck.errorCardsDue,
                                label = "Review",
                                color = MaterialTheme.colorScheme.error
                            )
                            DeckStatBadge(
                                count = deck.reviewCardsDue,
                                label = "Due",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Manage Deck") },
                        onClick = {
                            showMenu = false
                            deck.toDeckManagement()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Create Hard Cards Deck") },
                        onClick = {
                            showMenu = false
                            val deckName = homePageViewModel.createHardCardsDeck(deck.deckId, deck.deckName)
                            if (deckName != null) {
                                showHardDeckMessage = "Created: $deckName"
                                onRefresh()
                            } else {
                                showHardDeckMessage = "No hard cards found"
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AddCircle, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset Progress") },
                        onClick = {
                            showMenu = false
                            homePageViewModel.resetDeckForStudy(deck.todaysDeckId)
                            onRefresh()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Deck") },
                        onClick = {
                            showMenu = false
                            homePageViewModel.deleteDeck(deck.deckId)
                            onRefresh()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }

                showHardDeckMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DeckStatBadge(count: Int, label: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f),
            contentColor = color
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}
