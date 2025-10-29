package com.ameltz.languagelearner.ui.composable

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import kotlin.collections.forEach
import kotlin.uuid.Uuid

@Composable
fun HomePage(toNewDeck: () -> Unit, toManageDeck: (deckId: Uuid) -> Unit, homePageViewModel: HomePageViewModel,
             toCardManagement: () -> Unit) {
    LanguageLearnerTheme {
        Scaffold(topBar = {
            Text("Language Learner")
        }) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TopBanner(toNewDeck, toCardManagement)
                DeckDisplay(homePageViewModel.getAllDeckSummaries().map { dbDeck ->
                    dbDeck.deck.toHomePageDeckSummary({toManageDeck(dbDeck.deck.uuid)})
                })
            }
        }
    }
}

@Composable
fun TopBanner(toNewDeck: () -> Unit, toCardManagement: () -> Unit) {
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
                Text("ManageCards")
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
