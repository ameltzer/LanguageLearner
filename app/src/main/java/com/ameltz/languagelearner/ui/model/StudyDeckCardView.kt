package com.ameltz.languagelearner.ui.model

data class StudyDeckCardView(
    val front: String,
    val back: String,
    val easyCount: Int,
    val mediumCount: Int,
    val hardCount: Int,
    val isLearned: Boolean
)
