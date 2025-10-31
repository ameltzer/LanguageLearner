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
import com.ameltz.languagelearner.ui.viewmodel.AddCardViewModel
import com.ameltz.languagelearner.ui.viewmodel.AddCardsToDeckViewModel
import com.ameltz.languagelearner.ui.viewmodel.CardManagementViewModel
import com.ameltz.languagelearner.ui.viewmodel.DeckManagementViewModel
import com.ameltz.languagelearner.ui.viewmodel.HomePageViewModel
import com.ameltz.languagelearner.ui.viewmodel.NewDeckViewModel
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
    addCardsToDeckViewModel: AddCardsToDeckViewModel,
    cardManagementViewModel: CardManagementViewModel,
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
                toCardManagement = {navController.navigate(CardManagement)}
            )
        }
        composable<NewDeck> {
            AddDeck({navController.popBackStack()}, newDeckViewModel)
        }
        composable<ManageDeck>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) { backStackEntry ->
            val args = backStackEntry.toRoute<ManageDeck>()
            DeckManagement(deckManagementViewModel, args.deckId,
                {navController.popBackStack()},
                {deckId -> navController.navigate(AssociateCardsToDeck(deckId))}
            )
        }
        composable<AddCard>(typeMap = mapOf(typeOf<Uuid?>() to UuidOptionalNavType)) { backStackEntry ->
            val args = backStackEntry.toRoute<AddCard>()
            CardManagementComposable(addCardViewModel,
                { navController.popBackStack()},
                args.cardId

            )
        }
        composable<AssociateCardsToDeck>(typeMap = mapOf(typeOf<Uuid>() to UuidNavType)) { backStackEntry ->
            val args = backStackEntry.toRoute<AssociateCardsToDeck>()
            AddCardsToDeck(args.deckId, addCardsToDeckViewModel, {navController.popBackStack()},
                )
        }
        composable<CardManagement> {
            CardsManagement(cardManagementViewModel, {navController.popBackStack()},
                {cardId: Uuid -> navController.navigate(AddCard(cardId))},
                {navController.navigate(AddCard())})
        }
    }
}

