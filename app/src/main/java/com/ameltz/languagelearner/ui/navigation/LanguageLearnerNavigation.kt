package com.ameltz.languagelearner.ui.navigation

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
object LanguageLearnerHomePage
@Serializable
object NewDeck
@Serializable
data class ManageDeck (val deckId:Uuid)
@Serializable
data class AddCard(val cardId:Uuid?=null)
@Serializable
data class AssociateCardsToDeck(val deckId:Uuid)
@Serializable
object CardManagement