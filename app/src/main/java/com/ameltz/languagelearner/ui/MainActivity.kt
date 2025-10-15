package com.ameltz.languagelearner.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.navigation.NavControllerGraph
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import com.ameltz.languagelearner.ui.viewmodel.NewDeckViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity @Inject constructor(): ComponentActivity() {

    private val newDeckViewModel: NewDeckViewModel by viewModels()
    private val homePageViewModel: HomePageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageLearnerTheme {
                NavControllerGraph(newDeckViewModel = newDeckViewModel, homePageViewModel = homePageViewModel)
            }
        }
    }
}
