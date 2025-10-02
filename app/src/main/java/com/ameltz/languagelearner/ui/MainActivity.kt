package com.ameltz.languagelearner.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ameltz.languagelearner.ui.theme.LanguageLearnerTheme
import com.ameltz.languagelearner.ui.navigation.NavControllerGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageLearnerTheme {
                NavControllerGraph()
            }
        }
    }
}
