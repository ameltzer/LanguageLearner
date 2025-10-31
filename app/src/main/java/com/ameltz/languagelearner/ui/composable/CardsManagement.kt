package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.CardManagementViewModel
import kotlin.uuid.Uuid

@Composable
fun CardsManagement(cardManagementViewModel: CardManagementViewModel, back: () -> Unit,
                    toCard: (cardId: Uuid) -> Unit, toAddCard: () -> Unit) {

    val cards = cardManagementViewModel.getAllCards()

    LanguageLearnerTheme {
        Column {
            Row {
                Button(onClick = { toAddCard() }) {
                    Text("Create Card")
                }
                Button(onClick = { back() }) {
                    Text("Back")
                }
            }
            cards.forEach { card ->
                Row(modifier = Modifier.clickable(onClick = { toCard(card.uuid) })) {
                    Text(card.display())
                }
            }
        }
    }

}