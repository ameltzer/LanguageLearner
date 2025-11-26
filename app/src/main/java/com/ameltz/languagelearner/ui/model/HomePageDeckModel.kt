package com.ameltz.languagelearner.ui.model

import kotlin.uuid.Uuid


data class HomePageDeckModel(val deckName: String, var newCardsDue: Int, var reviewCardsDue: Int,
                             var errorCardsDue: Int, val toDeckManagement: () -> Unit,
                             val todaysDeckId: Uuid
)