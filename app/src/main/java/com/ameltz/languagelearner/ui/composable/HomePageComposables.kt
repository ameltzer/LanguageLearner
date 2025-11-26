package com.ameltz.languagelearner.ui.composable

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
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
fun HomePage(toNewDeck: () -> Unit,
             toManageDeck: (deckId: Uuid) -> Unit,
             homePageViewModel: HomePageViewModel,
             toCardManagement: () -> Unit,
             bulkImportViewModel: BulkImportViewModel,
             toStudyDeck: (studyDeckId: Uuid) -> Unit,
             toSettings: () -> Unit) {
    var isRefreshing by remember { mutableStateOf(false) }
    var decks by remember { mutableStateOf(homePageViewModel.getAllDeckSummaries(toManageDeck)) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            decks = homePageViewModel.getAllDeckSummaries(toManageDeck)
            delay(300) // Give UI time to show the refresh indicator
            isRefreshing = false
        }
    }
    val state = rememberPullToRefreshState()
    LanguageLearnerTheme {
        Scaffold(topBar = {
            Text("Language Learner")
        }) { padding ->
            PullToRefreshBox (
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = state,
                indicator = {
                    Indicator(isRefreshing = isRefreshing, state=state)
                }
            ) {
                Column(modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                ) {
                    TopBanner(toNewDeck, toCardManagement, bulkImportViewModel, toSettings, onRefresh)
                    DeckDisplay(decks, toStudyDeck, homePageViewModel, onRefresh)
                }
            }
        }
    }
}

@Composable
fun TopBanner(toNewDeck: () -> Unit, toCardManagement: () -> Unit, bulkImportViewModel: BulkImportViewModel,
              toSettings: () -> Unit, onRefresh: () -> Unit) {
    var fileContent by remember { mutableStateOf<String?>(null) }
    var deckName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Launcher for picking a file
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Read the file content
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        fileContent = reader.readText()
                    }
                }
                // Optionally get the file name
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            deckName = cursor.getString(nameIndex)
                        }

                    }
                }

// Fallback if query fails
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
                        println(parts)
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

    LanguageLearnerTheme {
        FlowRow  {
            Button(onClick = {
                    toNewDeck()
                }, modifier = Modifier.padding(Dp(6f))) {
                Text("New Deck")
            }
            Button(onClick = {
                toCardManagement()
            }, modifier = Modifier.padding(Dp(6f))) {
                Text("Manage Cards")
            }
            Button(onClick = {
                // Launch file picker with CSV MIME type filter
                filePicker.launch("*/*")
            }) {
                Text("Bulk import")
            }
            Button(onClick = {
                toSettings()
            }) {
                Text("Settings")
            }
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
    Column {
        decks.forEach { deck ->
            var showMenu by remember { mutableStateOf(false) }
            var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
            println("early: ${deck.todaysDeckId}")
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Dp(16f)).combinedClickable(
                        onClick = { toStudyDeck(deck.todaysDeckId) },
                        onLongClick = {
                            println("long click on ${deck.deckName}")
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        })
                ) {
                    Text(deck.deckName, color = Color.White)
                    Spacer(Modifier.weight(1f))
                    Text(
                        deck.newCardsDue.toString(),
                        color = Color.Blue,
                        modifier = Modifier.padding(Dp(4f))
                    )
                    Text(
                        deck.errorCardsDue.toString(),
                        color = Color.Red,
                        modifier = Modifier.padding(Dp(4f))
                    )
                    Text(
                        deck.reviewCardsDue.toString(),
                        color = Color.Green,
                        modifier = Modifier.padding(Dp(4f))
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = menuOffset
                ) {
                    DropdownMenuItem(
                        text = { Text("Reset deck for study") },
                        onClick = {
                            showMenu = false
                            homePageViewModel.resetDeckForStudy(deck.todaysDeckId)
                            onRefresh()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Manage Deck") },
                        onClick = {
                            showMenu = false
                            deck.toDeckManagement()
                        }
                    )
                }
            }
        }
    }
}
