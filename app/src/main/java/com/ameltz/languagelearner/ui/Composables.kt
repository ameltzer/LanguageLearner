package com.ameltz.languagelearner.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import kotlin.collections.forEach
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun HomePage(toNewDeck: () -> Unit, homePageViewModel: HomePageViewModel) {
    LanguageLearnerTheme {
        Scaffold(topBar = {
            Text("Language Learner")
        }) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TopBanner(toNewDeck)
                DeckDisplay(homePageViewModel.getAllDeckSummaries().map { dbDeck -> dbDeck.toHomePageDeckSummary() })
            }
        }
    }
}

@Composable
fun TopBanner(toNewDeck: () -> Unit) {
    LanguageLearnerTheme {
        Row {
            Button(onClick = {
                    println("New Deck")
                    toNewDeck()
                }, modifier = Modifier.padding(Dp(6f))) {
                Text("New Deck")
            }
            Button(onClick = { println("settings") }, modifier = Modifier.padding(Dp(6f))) {
                Text("Settings")
            }
            Button(onClick = { println("account") }, modifier = Modifier.padding(Dp(6f))) {
                Text("Account")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckDisplay(decks: List<HomePageDeckModel>) {
    val haptics = LocalHapticFeedback.current
    var selectedDeck by remember { mutableStateOf<HomePageDeckModel?>(null)}
    Column {
        decks.forEach { deck ->
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Dp(16f)).combinedClickable(
                        onClick = { deck.printName() },
                        onLongClick = {
                            println("long click on ${deck.getDeckName()}")
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedDeck = deck
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
                DropdownMenu(
                    expanded = selectedDeck == deck,
                    onDismissRequest = { selectedDeck = null }
                ) {
                    DeckActionContext(
                        deck = deck,
                    )
                }
            }
        }
    }
}

@Composable
fun DeckActionContext(deck: HomePageDeckModel) {
    Column {
        Text(modifier = Modifier.clickable(onClick = {println("add a card to ${deck.getDeckName()}")}), text="Add a card")
        Text(modifier = Modifier.clickable(onClick = {println("delete the deck ${deck.getDeckName()}")}), text="Delete deck")
    }
}