package com.ameltz.languagelearner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ameltz.languagelearner.data.entity.Setting
import com.ameltz.languagelearner.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    companion object {
        const val MEDIUM_TIME_DELAY_KEY = "medium_time_delay"
        const val HARD_TIME_DELAY_KEY = "hard_time_delay"
    }

    fun getMediumTimeDelay(): Int {
        return repository.getMediumTimeDelay()
    }

    fun getHardTimeDelay(): Int {
        return repository.getHardTimeDelay()
    }

    fun saveHardTimeDelay(hardTimeDelay: Int) {
        repository.saveHardTimeDelay(hardTimeDelay)
    }

    fun saveMediumTimeDelay(mediumTimeDelay: Int) {
        repository.saveMediumTimeDelay(mediumTimeDelay)
    }
}
