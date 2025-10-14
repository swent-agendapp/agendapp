package com.android.sample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.sample.ui.screens.EditEventScreen
import com.android.sample.ui.screens.HomeScreen

sealed class Screen(val route: String) {
  data object Home : Screen("home")

  data object EditEvent : Screen("edit_event/{eventId}") {
    fun createRoute(eventId: String) = "edit_event/$eventId"
  }
}

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
  NavHost(
      navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
          HomeScreen(
              onNavigateToEdit = { navController.navigate(Screen.EditEvent.createRoute("E001")) })
        }
        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
              val eventId = it.arguments!!.getString("eventId")!!
              EditEventScreen(eventId = eventId, onNavigateBack = { navController.popBackStack() })
            }
      }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
  val navController = rememberNavController()
  AppNavigation(navController)
}
