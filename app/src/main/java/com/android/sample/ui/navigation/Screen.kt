package com.android.sample.ui.navigation

/**
 * Represents all available destinations (screens) in the app. Each route corresponds to a
 * Composable destination in the NavHost.
 */
sealed class Screen(val route: String) {
  data object Home : Screen("home")

  data object Calendar : Screen("calendar")

  data object Map : Screen("map")

  data object Settings : Screen("settings")

  data object Profile : Screen("profile")

  data object AdminContact : Screen("admin_contact")

  data object AddEventTitle : Screen("add_event/title")

  data object AddEventTime : Screen("add_event/time")

  data object AddEventMember : Screen("add_event/member")

  data object AddEventEnd : Screen("add_event/end")

  data object EditEvent : Screen("edit_event/{eventId}") {
    fun createRoute(eventId: String) = "edit_event/$eventId"
  }
}
