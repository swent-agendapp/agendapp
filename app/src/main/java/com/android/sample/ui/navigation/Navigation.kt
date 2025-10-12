package com.android.sample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// TODO: Waiting for others to finish the UI design.
import com.android.sample.ui.screens.HomeScreen
import com.android.sample.ui.screens.EditEventScreen


// Navigation graph for the app.
// TODO: Can be updated according to UI design.
object Routes {
    const val HOME = "home"
    const val EDIT_EVENT = "edit_event"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onNavigateToEdit = { navController.navigate(Routes.EDIT_EVENT) })
        }
        composable(Routes.EDIT_EVENT) {
            EditEventScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}