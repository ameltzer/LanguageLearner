package com.ameltz.languagelearner.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.navigation.NavControllerGraph
import com.ameltz.languagelearner.ui.viewmodel.AddCardViewModel
import com.ameltz.languagelearner.ui.viewmodel.BulkImportViewModel
import com.ameltz.languagelearner.ui.viewmodel.CardManagementViewModel
import com.ameltz.languagelearner.ui.viewmodel.CardsManagementViewModel
import com.ameltz.languagelearner.ui.viewmodel.DeckManagementViewModel
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import com.ameltz.languagelearner.ui.viewmodel.NewDeckViewModel
import com.ameltz.languagelearner.ui.viewmodel.SettingsViewModel
import com.ameltz.languagelearner.ui.viewmodel.StudyViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity @Inject constructor(): ComponentActivity() {

    private val newDeckViewModel: NewDeckViewModel by viewModels()
    private val homePageViewModel: HomePageViewModel by viewModels()
    private val deckManagementViewModel: DeckManagementViewModel by viewModels()
    private val addCardViewModel: AddCardViewModel by viewModels()
    private val cardManagementViewModel: CardManagementViewModel by viewModels()
    private val cardsManagementViewModel: CardsManagementViewModel by viewModels()
    private val bulkImportViewModel: BulkImportViewModel by viewModels()
    private val studyViewModel: StudyViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageLearnerTheme {
                NavControllerGraph(newDeckViewModel = newDeckViewModel, homePageViewModel = homePageViewModel,
                    deckManagementViewModel = deckManagementViewModel, addCardViewModel = addCardViewModel,
                    cardManagementViewModel = cardManagementViewModel, cardsManagementViewModel = cardsManagementViewModel,
                    bulkImportViewModel=bulkImportViewModel, studyViewModel = studyViewModel,
                    settingsViewModel = settingsViewModel)
            }
        }
    }
}
