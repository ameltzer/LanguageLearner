package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardManagementViewModel @Inject constructor(val repository: Repository) : ViewModel(){

    fun getAllCards(): List<Card> {
        return repository.getAllCards()
    }

}