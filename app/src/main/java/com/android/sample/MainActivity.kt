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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.sample.ui.calendar.AddEventAttendantScreen
import com.android.sample.ui.calendar.AddEventConfirmationScreen
import com.android.sample.ui.calendar.AddEventTimeAndRecurrenceScreen
import com.android.sample.ui.calendar.AddEventTitleAndDescriptionScreen
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.Screen
import com.android.sample.ui.screens.HomeScreen
import com.android.sample.ui.settings.SettingsScreen
import com.android.sample.ui.theme.SampleAppTheme

object MainActivityTestTags {
  const val MAIN_SCREEN_CONTAINER = "main_screen_container"
}
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
            modifier =
                Modifier.fillMaxSize().semantics {
                  testTag = MainActivityTestTags.MAIN_SCREEN_CONTAINER
                },
            color = MaterialTheme.colorScheme.background) {
              Agendapp_Navigation()
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
      startDestination = Screen.Calendar.route,
      modifier = modifier) {
        navigation(startDestination = Screen.AddEventTitle.route, route = "Add Event") {
          composable(Screen.AddEventTitle.route) {
            AddEventTitleAndDescriptionScreen(
                onNext = { navigationActions.navigateTo(Screen.AddEventTime) })
          }
          composable(Screen.AddEventTime.route) {
            AddEventTimeAndRecurrenceScreen(
                onNext = { navigationActions.navigateTo(Screen.AddEventMember) })
          }
          composable(Screen.AddEventMember.route) {
            AddEventAttendantScreen(onCreate = { navigationActions.navigateTo(Screen.AddEventEnd) })
          }
          composable(Screen.AddEventEnd.route) {
            AddEventConfirmationScreen(onFinish = { navigationActions.navigateTo(Screen.Calendar) })
          }
        }
        navigation(startDestination = Screen.Settings.route, route = "Settings") {
          composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navigationActions.navigateBack() })
          }
        }
        navigation(startDestination = Screen.Home.route, route = "Home") {
          composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEdit = { eventId -> navigationActions.navigateToEditEvent(eventId) },
                onNavigateToCalendar = { navigationActions.navigateTo(Screen.Calendar) },
                onNavigateToSettings = { navigationActions.navigateTo(Screen.Settings) })
          }
        }
        navigation(startDestination = Screen.Calendar.route, route = "Calendar") {
          composable(Screen.Calendar.route) {
            CalendarScreen(onCreateEvent = { navigationActions.navigateTo(Screen.AddEventTitle) })
          }
        }
      }
}
