package com.android.sample.ui.navigation

/**
 * Centralized navigation actions for the app. Allows other Composable to navigate without directly
 * using NavController.
 */
sealed class Screen(val route: String, val name: String) {

  data object Authentication : Screen(route = "authentication", name = "Authentication")

  data object Home : Screen(route = "home", name = "Home")

  data object Calendar : Screen(route = "calendar", name = "Calendar")

  data object Map : Screen(route = "map", name = "Map")

  data object Settings : Screen(route = "settings", name = "Settings")

  data object Profile : Screen(route = "profile", name = "Profile")

  data object AdminContact : Screen(route = "admin_contact", name = "Admin Contact")

  data object AddEventTitle : Screen(route = "add_event/title", name = "Add Event Title")

  data object AddEventTime : Screen(route = "add_event/time", name = "Add Event Time")

  data object AddEventMember : Screen(route = "add_event/member", name = "Add Event Member")

  data object ReplacementPending : Screen("replacement/pending")

  data object EditEvent : Screen("edit_event/{eventId}") {
    fun createRoute(eventId: String) = "edit_event/$eventId"
  }
}
