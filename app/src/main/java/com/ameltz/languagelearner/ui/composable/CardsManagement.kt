package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.CardsManagementViewModel
import kotlin.uuid.Uuid

@Composable
fun CardsManagement(cardsManagementViewModel: CardsManagementViewModel, back: () -> Unit,
                    toCard: (cardId: Uuid) -> Unit, toAddCard: () -> Unit) {

    val cards = cardsManagementViewModel.getAllCards()

    LanguageLearnerTheme {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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