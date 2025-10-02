package com.ameltz.languagelearner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ameltz.languagelearner.ui.HomePage



@Composable
fun NavControllerGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = LanguageLearnerHomePage,
        modifier = modifier
    ) {
        composable<LanguageLearnerHomePage> {
            HomePage()
        }
    }
}

