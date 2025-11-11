package com.ameltz.languagelearner.ui.model

import com.ameltz.languagelearner.data.entity.Card

class AnkiCard(val front:String, val back:String) {
    fun toCard(): Card {
        return Card.createCard(front, back)
    }
}

class AnkiDeckImport(val deckName: String, val cards: List<AnkiCard>)