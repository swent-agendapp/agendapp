package com.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.sample.resources.C
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.Screen
import com.android.sample.ui.screens.EditEventScreen
import com.android.sample.ui.screens.HomeScreen
import com.android.sample.ui.settings.SettingsScreen
import com.android.sample.ui.theme.SampleAppTheme

/**
 * Main entry point of the application. Sets up the theme and calls [Agendapp_Navigation] to
 * initialize navigation.
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              Agendapp()
            }
      }
    }
  }
}

@Composable
fun Agendapp() {
  CalendarScreen()
}

/**
 * Root composable containing the navigation graph for the application. This function defines all
 * available routes and how composables are connected.
 */
@Composable
fun Agendapp_Navigation(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  NavHost(
      navController = navController,
      startDestination = Screen.Home.route,
      modifier = modifier.semantics { testTag = C.Tag.main_screen_container }) {
        // 🏠 Home screen
        composable(Screen.Home.route) {
          HomeScreen(
              onNavigateToEdit = { eventId -> navigationActions.navigateToEditEvent(eventId) },
              onNavigateToCalendar = { navigationActions.navigateTo(Screen.Calendar) },
              onNavigateToSettings = { navigationActions.navigateTo(Screen.Settings) })
        }

        // ✏️ Edit event screen (with eventId parameter)
        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { entry ->
              val eventId = entry.arguments?.getString("eventId") ?: ""
              EditEventScreen(
                  eventId = eventId, onNavigateBack = { navigationActions.navigateBack() })
            }

        // 📅 Calendar screen
        composable(Screen.Calendar.route) { CalendarScreen() }

        // ⚙️ Settings screen
        composable(Screen.Settings.route) {
          SettingsScreen(onNavigateBack = { navigationActions.navigateBack() })
        }
      }
}
