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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    studyDeckId: Uuid,
    studyViewModel: StudyViewModel,
    onNavigateBack: () -> Unit,
    onEditCard: (Uuid) -> Unit
) {
    val studyDeck = studyViewModel.loadStudyDeck(studyDeckId)

    LanguageLearnerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (studyDeck.cards.isNotEmpty()) {
                            Text(
                                "Card ${studyViewModel.currentCardIndex + 1} of ${studyDeck.cards.size}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        } else {
                            Text("Study Session")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (studyDeck.cards.isNotEmpty() && !studyViewModel.isDone(studyDeckId)) {
                            IconButton(
                                onClick = {
                                    val currentCard = studyDeck.cards[studyViewModel.currentCardIndex]
                                    onEditCard(currentCard.cardInDeck.card.uuid)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Card"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Progress indicator
                if (studyDeck.cards.isNotEmpty()) {
                    val progress = (studyViewModel.currentCardIndex + 1).toFloat() / studyDeck.cards.size
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Study session complete!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Great job! All cards reviewed.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(onClick = onNavigateBack) {
                                    Text(text = "Back to Home")
                                }
                            }
                        }
                        studyDeck.cards.isEmpty() -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "No cards to study",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Come back later or add more cards to this deck",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(onClick = onNavigateBack) {
                                    Text(text = "Back to Home")
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
                Button(
                    onClick = { onDifficultySelected(CardDifficulty.HARD) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Hard")
                }
                Spacer(modifier = Modifier.padding(4.dp))
                Button(
                    onClick = { onDifficultySelected(CardDifficulty.MEDIUM) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Medium")
                }
                Spacer(modifier = Modifier.padding(4.dp))
                Button(
                    onClick = { onDifficultySelected(CardDifficulty.EASY) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Easy")
                }
            }
        } else {
            Text(
                text = "Tap card to reveal answer",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
