package com.ameltz.languagelearner.ui.model

class HomePageDeckModel(private val deckName: String, private var newCardsDue: Int, private var reviewCardsDue: Int, private var errorCardsDue: Int) {

    fun printName() {
        println(this.deckName)
    }

    fun getDeckName(): String {
        return this.deckName;
    }

    fun getNewCardsDue(): Int {
        return this.newCardsDue;
    }

    fun getReviewCardsDue(): Int {
        return this.reviewCardsDue
    }

    fun getErrorCardsDue(): Int {
        return this.errorCardsDue;
    }


}