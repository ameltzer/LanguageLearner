package com.ameltz.languagelearner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ameltz.languagelearner.data.repository.Repository
import com.ameltz.languagelearner.ui.AddDeck
import com.ameltz.languagelearner.ui.HomePage
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import com.ameltz.languagelearner.ui.viewmodel.NewDeckViewModel


@Composable
fun NavControllerGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    newDeckViewModel: NewDeckViewModel,
    homePageViewModel: HomePageViewModel
) {
    NavHost(
        navController = navController,
        startDestination = LanguageLearnerHomePage,
        modifier = modifier
    ) {
        composable<LanguageLearnerHomePage> {
            HomePage({navController.navigate(NewDeck)}, homePageViewModel = homePageViewModel)
        }
        composable<NewDeck> {
            AddDeck({navController.navigate(LanguageLearnerHomePage)}, newDeckViewModel)
        }
    }
}

