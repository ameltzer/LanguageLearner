package com.ameltz.languagelearner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageLearnerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun TopBanner() {
    LanguageLearnerTheme {
        Row {
            Button(onClick = { print("New Deck")}) {
                Text("New Deck")
            }
            Button(onClick = { print("New card")}) {
                Text("New Card")
            }
            Button(onClick = { print("settings")}) {
                Text("Settings")
            }
            Button(onClick = {print("account")}) {
                Text("account")
            }
        }
    }
}

@Composable
fun DeckDisplay(decks: List<HomePageDeckView>) {

    Row {
        decks.forEach { deck:HomePageDeckView ->
            Button(onClick = { deck.printName()}) {
                Text(deck.getDeckName())
            }
        }
    }


}