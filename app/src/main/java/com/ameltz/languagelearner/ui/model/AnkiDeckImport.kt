package com.ameltz.languagelearner.ui.model

import com.ameltz.languagelearner.data.entity.Card
import kotlin.uuid.Uuid

class AnkiCard(val front: String, val back: String, val categorization: List<String> = emptyList()) {
    fun toCard(): Card {
        return Card(Uuid.random(), front, back, categorization)
    }
}

class AnkiDeckImport(val deckName: String, val cards: List<AnkiCard>)