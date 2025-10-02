package com.ameltz.languagelearner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.ameltz.languagelearner.ui.model.HomePageDeckModel
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import kotlin.collections.forEach


@Composable
fun HomePage() {
    LanguageLearnerTheme {
        Scaffold(topBar = {
            Text("Language Learner")
        }) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TopBanner()
                DeckDisplay(listOf(HomePageDeckModel("Japanese to English", 0, 0, 0)))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBanner() {
    LanguageLearnerTheme {
        Row {
            Button(onClick = { println("New Deck") }, modifier = Modifier.padding(Dp(6f))) {
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

@Composable
fun DeckDisplay(decks: List<HomePageDeckModel>) {

    Row {
        decks.forEach { deck: HomePageDeckModel ->
            Row(modifier = Modifier.fillMaxWidth().padding(Dp(16f)).clickable(onClick = {deck.printName()}) ) {
                Text(deck.getDeckName(), color = Color.White)
                Spacer(Modifier.weight(1f))
                Text(deck.getNewCardsDue().toString(), color = Color.Blue, modifier = Modifier.padding(Dp(4f)))
                Text(deck.getErrorCardsDue().toString(), color = Color.Red, modifier = Modifier.padding(Dp(4f)))
                Text(deck.getReviewCardsDue().toString(), color = Color.Green, modifier = Modifier.padding(Dp(4f)))

            }
        }
    }
}