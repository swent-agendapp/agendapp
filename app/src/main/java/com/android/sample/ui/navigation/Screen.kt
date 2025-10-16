package com.android.sample.ui.navigation

/**
 * Represents all available destinations (screens) in the app. Each route corresponds to a
 * Composable destination in the NavHost.
 */
sealed class Screen(val route: String) {
  data object Home : Screen("home")

  data object Calendar : Screen("calendar")

  data object Settings : Screen("settings")

  data object EditEvent : Screen("edit_event/{eventId}") {
    fun createRoute(eventId: String) = "edit_event/$eventId"
  }
}
