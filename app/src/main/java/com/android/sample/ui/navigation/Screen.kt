package com.android.sample.ui.navigation

/**
 * Centralized navigation actions for the app. Allows other Composable to navigate without directly
 * using NavController.
 */
sealed class Screen(val route: String, val name: String) {

  data object Authentication : Screen(route = "authentication", name = "Authentication")

  data object Calendar : Screen(route = "calendar", name = "Calendar")

  data object EventOverview : Screen(route = "event_overview/{eventId}", name = "Event Overview") {
    fun createRoute(eventId: String) = "event_overview/$eventId"
  }

  data object Map : Screen(route = "map", name = "Map")

  data object Settings : Screen(route = "settings", name = "Settings")

  data object Profile : Screen(route = "profile", name = "Profile")

  data object AdminContact : Screen(route = "admin_contact", name = "Admin Contact")

  data object AddEvent : Screen(route = "add_event", name = "Add Event")

  data object ReplacementOverview : Screen("replacement_overview", name = "Replacement Overview")

  data object ReplacementOrganize : Screen("replacement_organize", name = "Replacement Organize")

  data object ReplacementPending : Screen("replacement/pending", name = "Replacement Pending")

  data object EditEvent : Screen("edit_event/{eventId}", name = "Edit Event") {
    fun createRoute(eventId: String) = "edit_event/$eventId"
  }
}
