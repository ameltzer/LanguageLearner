package com.ameltz.languagelearner.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.ui.viewmodel.CardManagementViewModel
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardsToDeck(
    deckId: Uuid,
    cardManagementViewModel: CardManagementViewModel,
    back: () -> Unit,
    modifier: Modifier = Modifier
) {
    var items by remember { mutableStateOf<List<Card>>(emptyList()) }
    var selectedItems by remember { mutableStateOf<Set<Card>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var displayName by remember { mutableStateOf("Deck Name") }
    var sortOption by remember { mutableStateOf(SortOption.NONE) }

    LaunchedEffect(deckId) {
        val deck = cardManagementViewModel.getDeck(deckId)
        items = cardManagementViewModel.getAllCards()
        if (deck != null) {
            val cardInDeck = deck.cardsInDeck.map { it.card.uuid }
            items.forEach { card ->
                if (cardInDeck.contains(card.uuid)) {
                    selectedItems = selectedItems + card
                }
            }
        }
        isLoading = false
        displayName = deck?.deck?.name ?: ""
    }

    if (displayName == "") {
        Button(onClick = { back() }) {
            Text(text = "Go back")
        }
        return
    }

    val sortedItems = when (sortOption) {
        SortOption.BY_FRONT -> items.sortedBy { it.front.lowercase() }
        SortOption.BY_BACK -> items.sortedBy { it.back.lowercase() }
        SortOption.BY_ENGLISH -> items.sortedBy { card ->
            val latinCharCountFront = card.front.count { it.code in 0..127 }
            val englishText = if (latinCharCountFront > card.front.length / 2) card.front else card.back
            englishText.lowercase()
        }
        SortOption.NONE -> items
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Cards to $displayName",
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
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Selection summary
                Text(
                    text = "${selectedItems.size} of ${items.size} cards selected",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Sort options
                Row(
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
                }

                // Cards list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedItems) { item ->
                        SelectableCardRow(
                            card = item,
                            isSelected = selectedItems.contains(item),
                            onSelect = {
                                selectedItems = if (selectedItems.contains(item)) {
                                    selectedItems - item
                                } else {
                                    selectedItems + item
                                }
                            }
                        )
                    }
                }

                // Save button
                Button(
                    onClick = {
                        cardManagementViewModel.addCardsToDeck(
                            deckId,
                            selectedItems.map { it.uuid })
                        val cardsToRemove =
                            items.filter { !selectedItems.contains(it) }.map { it.uuid }
                        cardManagementViewModel.removeCardsFromDeck(deckId, cardsToRemove)
                        back()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
fun SelectableCardRow(
    card: Card,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.back,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


