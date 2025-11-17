package com.android.sample.ui.navigation

import androidx.navigation.NavHostController

/**
 * Centralized navigation actions for the app. Allows other Composables to navigate without directly
 * using NavController.
 */
class NavigationActions(val navController: NavHostController) {

  /** Navigate to a simple destination (without parameters). */
  fun navigateTo(screen: Screen) {
    navController.navigate(screen.route)
  }

  /** Navigate to EditEvent screen with eventId parameter. */
  fun navigateToEditEvent(eventId: String) {
    navController.navigate(Screen.EditEvent.createRoute(eventId))
  }

  /** Navigate to EventOverview screen with eventId parameter. */
  fun navigateToEventOverview(eventId: String) {
    navController.navigate(Screen.EventOverview.createRoute(eventId))
  }

  /** Navigate back to previous screen. */
  fun navigateBack() {
    navController.popBackStack()
  }
}
