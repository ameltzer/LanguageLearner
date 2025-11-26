package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.CardDifficulty
import com.ameltz.languagelearner.ui.viewmodel.StudyViewModel
import kotlin.uuid.Uuid

@Composable
fun StudyScreen(
    studyDeckId: Uuid,
    studyViewModel: StudyViewModel,
    onNavigateBack: () -> Unit
) {
    val studyDeck = studyViewModel.loadStudyDeck(studyDeckId)

    LanguageLearnerTheme {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Progress indicator
                if (studyDeck.cards.isNotEmpty()) {
                    val progress = (studyViewModel.currentCardIndex + 1).toFloat() / studyDeck.cards.size
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Card ${studyViewModel.currentCardIndex + 1} of ${studyDeck.cards.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // Flashcard content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        studyViewModel.isDone(studyDeckId) -> {
                            Column {
                                Text(
                                    text = "Study session complete!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Button(onClick = onNavigateBack) {
                                    Text(text = "Go back")
                                }
                            }
                        }
                        studyDeck.cards.isEmpty() -> {
                            Column {
                                Text(
                                    text = "No cards to study. Come back later",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Button(onClick = onNavigateBack) {
                                    Text(text = "Go back")
                                }
                            }
                        }
                        else -> {
                            val currentCard = studyDeck.cards[studyViewModel.currentCardIndex]
                            FlashCard(
                                front = currentCard.cardInDeck.card.front,
                                back = currentCard.cardInDeck.card.back,
                                isFlipped = studyViewModel.isFlipped,
                                onCardClick = { studyViewModel.flipCard() },
                                onDifficultySelected = { difficulty ->
                                    studyViewModel.updateCurrentCard(currentCard, difficulty)
                                    if (studyViewModel.currentCardIndex == studyDeck.cards.size-1) {
                                        onNavigateBack()
                                    } else {
                                        studyViewModel.nextCard()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlashCard(
    front: String,
    back: String,
    isFlipped: Boolean,
    onCardClick: () -> Unit,
    onDifficultySelected: (CardDifficulty) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clickable(enabled = !isFlipped) { onCardClick() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFlipped) back else front,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }

        // Difficulty buttons (only shown when card is flipped)
        if (isFlipped) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DifficultyButton(
                    text = "Hard",
                    color = Color.Red,
                    onClick = { onDifficultySelected(CardDifficulty.HARD) }
                )
                DifficultyButton(
                    text = "Medium",
                    color = Color.Yellow,
                    onClick = { onDifficultySelected(CardDifficulty.MEDIUM) }
                )
                DifficultyButton(
                    text = "Easy",
                    color = Color.Green,
                    onClick = { onDifficultySelected(CardDifficulty.EASY) }
                )
            }
        } else {
            // Instruction text when card is not flipped
            Text(
                text = "Tap to reveal answer",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun DifficultyButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.Black,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
