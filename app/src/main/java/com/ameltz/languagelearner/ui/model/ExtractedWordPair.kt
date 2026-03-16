package com.ameltz.languagelearner.ui.model

import android.net.Uri

data class ExtractedWordPair(
    val japanese: String,
    val english: String,
    val id: String = java.util.UUID.randomUUID().toString()
)

data class ImageWordExtraction(
    val imageUri: Uri,
    val wordPairs: List<ExtractedWordPair>,
    val timestamp: Long = System.currentTimeMillis()
)
