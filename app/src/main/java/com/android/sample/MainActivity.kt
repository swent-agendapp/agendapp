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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.sample.ui.calendar.AddEventAttendantScreen
import com.android.sample.ui.calendar.AddEventConfirmationScreen
import com.android.sample.ui.calendar.AddEventTimeAndRecurrenceScreen
import com.android.sample.ui.calendar.AddEventTitleAndDescriptionScreen
import com.android.sample.ui.calendar.AddEventViewModel
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.map.MapScreen
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.Screen
import com.android.sample.ui.screens.HomeScreen
import com.android.sample.ui.settings.SettingsScreen
import com.android.sample.ui.theme.SampleAppTheme

object MainActivityTestTags {
  const val MAIN_SCREEN_CONTAINER = "main_screen_container"
}
/**
 * Main entry point of the application. Sets up the theme and calls [AgendappNavigation] to
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
              AgendappNavigation()
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
fun AgendappNavigation(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val addEventViewModel: AddEventViewModel = viewModel()

  NavHost(navController = navController, startDestination = Screen.Map.route, modifier = modifier) {
    navigation(startDestination = Screen.AddEventTitle.route, route = "Add Event") {
      composable(Screen.AddEventTitle.route) {
        AddEventTitleAndDescriptionScreen(
            addEventViewModel = addEventViewModel,
            onNext = { navigationActions.navigateTo(Screen.AddEventTime) },
            onCancel = {
              navigationActions.navigateBack()
              addEventViewModel.resetUiState()
            })
      }
      composable(Screen.AddEventTime.route) {
        AddEventTimeAndRecurrenceScreen(
            addEventViewModel = addEventViewModel,
            onNext = { navigationActions.navigateTo(Screen.AddEventMember) },
            onBack = { navigationActions.navigateBack() })
      }
      composable(Screen.AddEventMember.route) {
        AddEventAttendantScreen(
            addEventViewModel = addEventViewModel,
            onCreate = { navigationActions.navigateTo(Screen.AddEventEnd) },
            onBack = { navigationActions.navigateBack() })
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
    navigation(startDestination = Screen.Map.route, route = "Map") {
      composable(Screen.Map.route) { MapScreen() }
    }
  }
}
