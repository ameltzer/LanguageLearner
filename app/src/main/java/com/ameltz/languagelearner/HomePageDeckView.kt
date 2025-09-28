package com.ameltz.languagelearner

class HomePageDeckView(private val deckName: String, private var newCardsDue: Integer, private var reviewCardsDue: Integer) {

    fun printName() {
        println(this.deckName)
    }

    fun getDeckName(): String {
        return this.deckName;
    }
}