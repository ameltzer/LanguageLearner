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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import com.ameltz.languagelearner.ui.model.AnkiCard
import com.ameltz.languagelearner.ui.model.AnkiDeckImport
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.BulkImportViewModel
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Arrays
import kotlin.collections.forEach
import kotlin.uuid.Uuid

@Composable
fun HomePage(toNewDeck: () -> Unit, toManageDeck: (deckId: Uuid) -> Unit, homePageViewModel: HomePageViewModel,
             toCardManagement: () -> Unit, bulkImportViewModel: BulkImportViewModel) {
    LanguageLearnerTheme {
        Scaffold(topBar = {
            Text("Language Learner")
        }) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TopBanner(toNewDeck, toCardManagement, bulkImportViewModel)
                DeckDisplay(homePageViewModel.getAllDeckSummaries().map { dbDeck ->
                    dbDeck.deck.toHomePageDeckSummary({toManageDeck(dbDeck.deck.uuid)})
                })
            }
        }
    }
}

@Composable
fun TopBanner(toNewDeck: () -> Unit, toCardManagement: () -> Unit, bulkImportViewModel: BulkImportViewModel) {
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

            } catch (e: Exception) {
                e.printStackTrace()
                deckName = null
            }
        }
    }

    LanguageLearnerTheme {
        FlowRow  {
            Button(onClick = {
                    println("New Deck")
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
                Text("Select File for bulk import")
            }
        }
    }
}

@Composable
fun DeckDisplay(decks: List<HomePageDeckModel>) {
    val haptics = LocalHapticFeedback.current
    Column {
        decks.forEach { deck ->
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Dp(16f)).combinedClickable(
                        onClick = { deck.printName() },
                        onLongClick = {
                            println("long click on ${deck.getDeckName()}")
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            val toDeckManagement = deck.getToDeckManagement()
                            toDeckManagement()
                        })
                ) {
                    Text(deck.getDeckName(), color = Color.White)
                    Spacer(Modifier.weight(1f))
                    Text(
                        deck.getNewCardsDue().toString(),
                        color = Color.Blue,
                        modifier = Modifier.padding(Dp(4f))
                    )
                    Text(
                        deck.getErrorCardsDue().toString(),
                        color = Color.Red,
                        modifier = Modifier.padding(Dp(4f))
                    )
                    Text(
                        deck.getReviewCardsDue().toString(),
                        color = Color.Green,
                        modifier = Modifier.padding(Dp(4f))
                    )
                }
            }
        }
    }
}
