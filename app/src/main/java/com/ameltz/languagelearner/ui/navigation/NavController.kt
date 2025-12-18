package com.ameltz.languagelearner.ui.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ameltz.languagelearner.ui.composable.CardManagementComposable
import com.ameltz.languagelearner.ui.composable.AddCardsToDeck
import com.ameltz.languagelearner.ui.composable.AddDeck
import com.ameltz.languagelearner.ui.composable.CardsManagement
import com.ameltz.languagelearner.ui.composable.DeckManagement
import com.ameltz.languagelearner.ui.composable.HomePage
import com.ameltz.languagelearner.ui.composable.SettingsPage
import com.ameltz.languagelearner.ui.composable.StudyScreen
import com.ameltz.languagelearner.ui.viewmodel.AddCardViewModel
import com.ameltz.languagelearner.ui.viewmodel.BulkImportViewModel
import com.ameltz.languagelearner.ui.viewmodel.CardManagementViewModel
import com.ameltz.languagelearner.ui.viewmodel.CardsManagementViewModel
import com.ameltz.languagelearner.ui.viewmodel.DeckManagementViewModel
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import com.ameltz.languagelearner.ui.viewmodel.NewDeckViewModel
import com.ameltz.languagelearner.ui.viewmodel.SettingsViewModel
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

// Custom NavType for kotlin.uuid.Uuid
val UuidNavType = object : NavType<Uuid>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Uuid? {
        // Get the string from the bundle and convert it to a Uuid
        val ser = bundle.getString(key)
        if(ser == null) {
            return null;
        }
        return Uuid.parse(ser)
    }

    override fun parseValue(value: String): Uuid {
        // Parse the string value from the route into a Uuid
        return Uuid.parse(value)
    }

    override fun put(bundle: Bundle, key: String, value: Uuid) {
        // Put the Uuid into the bundle as a string
        bundle.putString(key, value.toString())
    }
}

val UuidOptionalNavType = object : NavType<Uuid?>(isNullableAllowed = true) {
    override fun get(bundle: Bundle, key: String): Uuid? {
        // Get the string from the bundle and convert it to a Uuid
        val ser = bundle.getString(key)
        if(ser == null) {
            return null;
        }
        return Uuid.parse(ser)
    }

    override fun parseValue(value: String): Uuid? {
        if(value == "") {
            return null
        }
        // Parse the string value from the route into a Uuid
        return Uuid.parse(value)
    }

    override fun put(bundle: Bundle, key: String, value: Uuid?) {
        // Put the Uuid into the bundle as a string
        bundle.putString(key, value?.toString())
    }
}

@Composable
fun NavControllerGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    newDeckViewModel: NewDeckViewModel,
    homePageViewModel: HomePageViewModel,
    deckManagementViewModel: DeckManagementViewModel,
    addCardViewModel: AddCardViewModel,
    cardManagementViewModel: CardManagementViewModel,
    cardsManagementViewModel: CardsManagementViewModel,
    bulkImportViewModel: BulkImportViewModel,
    studyViewModel: com.ameltz.languagelearner.ui.viewmodel.StudyViewModel,
    settingsViewModel: SettingsViewModel
) {

    NavHost(
        navController = navController,
        startDestination = LanguageLearnerHomePage,
        modifier = modifier
    ) {
        composable<LanguageLearnerHomePage> {
            HomePage(
                toNewDeck={navController.navigate(NewDeck)},
                homePageViewModel = homePageViewModel,
                toManageDeck = {deckId -> navController.navigate(ManageDeck(deckId))},
                toCardManagement = {navController.navigate(CardsManagement)},
                bulkImportViewModel=bulkImportViewModel,
                toStudyDeck = {studyDeckId -> navController.navigate(StudyDeck(studyDeckId))},
                toSettings = {navController.navigate(Settings)}
            )
        }
        composable<NewDeck> {
            AddDeck({navController.navigate(LanguageLearnerHomePage)}, newDeckViewModel)
        }
        composable<ManageDeck>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) { backStackEntry ->
            val args = backStackEntry.toRoute<ManageDeck>()
            DeckManagement(deckManagementViewModel, args.deckId,
                {navController.popBackStack()},
                {deckId -> navController.navigate(AssociateCardsToDeck(deckId))},
                {cardId: Uuid -> navController.navigate(CardManagement(cardId))}
            )
        }
        composable<CardManagement>(typeMap = mapOf(typeOf<Uuid?>() to UuidOptionalNavType)) { backStackEntry ->
            val args = backStackEntry.toRoute<CardManagement>()
            CardManagementComposable(addCardViewModel,
                { navController.navigate(LanguageLearnerHomePage)},
                args.cardId

            )
        }
        composable<AssociateCardsToDeck>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) { backStackEntry ->
            val args = backStackEntry.toRoute<AssociateCardsToDeck>()
            AddCardsToDeck(args.deckId, cardManagementViewModel, {navController.navigate(LanguageLearnerHomePage)},
                )
        }
        composable<CardsManagement> {
            CardsManagement(cardsManagementViewModel, {navController.navigate(LanguageLearnerHomePage)},
                {cardId: Uuid -> navController.navigate(CardManagement(cardId))},
                {navController.navigate(CardManagement())})
        }
        composable<StudyDeck>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) { backStackEntry ->
            val args = backStackEntry.toRoute<StudyDeck>()
            StudyScreen(
                studyDeckId = args.studyDeckId,
                studyViewModel = studyViewModel,
                onNavigateBack = { navController.navigate(LanguageLearnerHomePage) },
                onEditCard = { cardId -> navController.navigate(CardManagement(cardId)) }
            )
        }
        composable<Settings> {
            SettingsPage(
                toHomePage = { navController.navigate(LanguageLearnerHomePage) },
                settingsViewModel = settingsViewModel
            )
        }
    }
}

