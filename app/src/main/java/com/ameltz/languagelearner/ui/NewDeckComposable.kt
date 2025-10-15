package com.ameltz.languagelearner.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.ameltz.languagelearner.data.entity.Deck
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.NewDeckViewModel
import java.util.UUID

@Composable
fun AddDeck(toHomePage: () -> Unit, newDeckViewModel: NewDeckViewModel) {

    var deckName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    LanguageLearnerTheme {
        Row {
            TextField(
                value = deckName,
                onValueChange = {deckName = it},
                label = { Text("Deck Name")}
            )
            Button(onClick = {
                println("added!")
                newDeckViewModel.createNewDeck(
                    Deck(
                        UUID.randomUUID(),
                        deckName.text,
                        UUID.fromString("10c0eca2-f236-423e-9c23-04bcce7450e6")
                    )
                )
                toHomePage()
            }) {
                Text("Save")
            }
        }
    }
}
