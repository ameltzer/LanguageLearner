package com.ameltz.languagelearner.ui.model

import kotlin.uuid.Uuid

data class VoiceCardData(
    val front: String,
    val back: String,
    val deckName: String,
    val matchedDeckId: Uuid? = null,
    val confidence: Float = 1.0f
)

data class VoiceCardAnalysisResult(
    val success: Boolean,
    val cardData: VoiceCardData? = null,
    val errorMessage: String? = null,
    val rawTranscript: String? = null
)
