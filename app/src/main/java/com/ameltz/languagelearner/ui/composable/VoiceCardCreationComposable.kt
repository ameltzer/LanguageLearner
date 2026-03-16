package com.ameltz.languagelearner.ui.composable

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ameltz.languagelearner.ui.model.VoiceCardData
import com.ameltz.languagelearner.ui.viewmodel.VoiceCardCreationViewModel
import com.ameltz.languagelearner.ui.viewmodel.VoiceCardState
import java.util.Locale

@Composable
fun VoiceCardCreationButton(
    viewModel: VoiceCardCreationViewModel,
    apiKey: String,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showPermissionDenied by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            showPermissionDenied = true
        }
    }

    // Speech recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { data ->
            val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val transcript = matches?.firstOrNull()
            if (transcript != null) {
                viewModel.processVoiceTranscript(transcript, apiKey)
            } else {
                viewModel.resetState()
            }
        } ?: run {
            viewModel.resetState()
        }
    }

    // Launch speech recognizer when state changes to Listening
    LaunchedEffect(state) {
        if (state is VoiceCardState.Listening) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say: Add card [word] meaning [translation] to [deck]")
            }
            speechLauncher.launch(intent)
        }
    }

    FilledTonalButton(
        onClick = {
            if (apiKey.isBlank()) {
                showPermissionDenied = true
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        modifier = modifier,
        enabled = state !is VoiceCardState.Processing
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.padding(4.dp))
        when (state) {
            is VoiceCardState.Processing -> Text("Processing...")
            else -> Text("Voice")
        }
    }

    // Show confirmation dialog
    when (val currentState = state) {
        is VoiceCardState.ConfirmationNeeded -> {
            VoiceCardConfirmationDialog(
                cardData = currentState.cardData,
                transcript = currentState.transcript,
                onConfirm = { updatedData ->
                    viewModel.createCard(updatedData)
                },
                onDismiss = {
                    viewModel.resetState()
                }
            )
        }
        is VoiceCardState.Success -> {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1000)
                viewModel.resetState()
                onSuccess()
            }
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                title = { Text("Success") },
                text = { Text(currentState.message) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetState()
                        onSuccess()
                    }) {
                        Text("OK")
                    }
                }
            )
        }
        is VoiceCardState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                title = { Text("Error") },
                text = { Text(currentState.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetState() }) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }

    // Permission denied dialog
    if (showPermissionDenied) {
        AlertDialog(
            onDismissRequest = { showPermissionDenied = false },
            title = { Text("Permission Required") },
            text = {
                Text(
                    if (apiKey.isBlank()) {
                        "Please configure your Anthropic API key in Settings first."
                    } else {
                        "Microphone permission is required for voice input. Please grant permission in Settings."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showPermissionDenied = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun VoiceCardConfirmationDialog(
    cardData: VoiceCardData,
    transcript: String,
    onConfirm: (VoiceCardData) -> Unit,
    onDismiss: () -> Unit
) {
    var editedFront by remember { mutableStateOf(cardData.front) }
    var editedBack by remember { mutableStateOf(cardData.back) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Card") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Voice command: \"$transcript\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = editedFront,
                    onValueChange = { editedFront = it },
                    label = { Text("Front") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )

                OutlinedTextField(
                    value = editedBack,
                    onValueChange = { editedBack = it },
                    label = { Text("Back") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Deck:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        cardData.deckName + if (cardData.matchedDeckId != null) " ✓" else " (not found)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (cardData.matchedDeckId != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        cardData.copy(
                            front = editedFront,
                            back = editedBack
                        )
                    )
                },
                enabled = editedFront.isNotBlank() && editedBack.isNotBlank() && cardData.matchedDeckId != null
            ) {
                Text("Create Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
