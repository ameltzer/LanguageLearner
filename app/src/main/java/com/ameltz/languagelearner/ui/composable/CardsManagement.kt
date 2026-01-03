package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.viewmodel.CardsManagementViewModel
import kotlin.uuid.Uuid

enum class SortOption {
    NONE,
    BY_FRONT,
    BY_BACK,
    BY_ENGLISH
}

// Helper function to check if text is primarily English (Latin characters)
private fun isEnglish(text: String): Boolean {
    val latinCharCount = text.count { it.code in 0..127 }
    return latinCharCount > text.length / 2
}

// Helper function to get English text from card (either front or back)
private fun getEnglishText(card: com.ameltz.languagelearner.data.entity.Card): String {
    return if (isEnglish(card.front)) card.front else card.back
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsManagement(
    cardsManagementViewModel: CardsManagementViewModel,
    back: () -> Unit,
    toCard: (cardId: Uuid) -> Unit,
    toAddCard: () -> Unit
) {
    var sortOption by remember { mutableStateOf(SortOption.NONE) }
    var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    val allCards = cardsManagementViewModel.getAllCards()

    val sortedCards = when (sortOption) {
        SortOption.BY_FRONT -> allCards.sortedBy { it.front.lowercase() }
        SortOption.BY_BACK -> allCards.sortedBy { it.back.lowercase() }
        SortOption.BY_ENGLISH -> allCards.sortedBy { getEnglishText(it).lowercase() }
        SortOption.NONE -> allCards
    }

    val cards = if (searchQuery.text.isNotBlank()) {
        sortedCards.filter { card ->
            card.front.contains(searchQuery.text, ignoreCase = true) ||
            card.back.contains(searchQuery.text, ignoreCase = true)
        }
    } else {
        sortedCards
    }

    LanguageLearnerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Manage Cards",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = back) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = toAddCard,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Card")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Sort options
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = sortOption == SortOption.NONE,
                        onClick = { sortOption = SortOption.NONE },
                        label = { Text("Default") },
                        leadingIcon = if (sortOption == SortOption.NONE) {
                            { Icon(Icons.Default.Clear, contentDescription = null) }
                        } else null
                    )
                    FilterChip(
                        selected = sortOption == SortOption.BY_FRONT,
                        onClick = { sortOption = SortOption.BY_FRONT },
                        label = { Text("Sort by Front") }
                    )
                    FilterChip(
                        selected = sortOption == SortOption.BY_BACK,
                        onClick = { sortOption = SortOption.BY_BACK },
                        label = { Text("Sort by Back") }
                    )
                    FilterChip(
                        selected = sortOption == SortOption.BY_ENGLISH,
                        onClick = { sortOption = SortOption.BY_ENGLISH },
                        label = { Text("Sort by English") }
                    )
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search cards") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    trailingIcon = if (searchQuery.text.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    } else null
                )

                // Card list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards) { card ->
                        Card(
                            onClick = { toCard(card.uuid) },
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = card.front,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = card.back,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}