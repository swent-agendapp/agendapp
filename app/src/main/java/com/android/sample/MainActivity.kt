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
import com.android.sample.model.organization.EmployeeRepositoryFirebase
import com.android.sample.model.organization.EmployeeRepositoryProvider
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.calendar.addEvent.AddEventScreen
import com.android.sample.ui.map.MapScreen
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.Screen
import com.android.sample.ui.profile.AdminContactScreen
import com.android.sample.ui.profile.ProfileScreen
import com.android.sample.ui.replacement.ReplacementPendingListScreen
import com.android.sample.ui.replacement.ReplacementScreen
import com.android.sample.ui.screens.HomeScreen
import com.android.sample.ui.settings.SettingsScreen
import com.android.sample.ui.theme.SampleAppTheme
import com.github.se.bootcamp.model.authentication.AuthRepositoryFirebase
import com.google.firebase.firestore.FirebaseFirestore

object MainActivityTestTags {
  const val MAIN_SCREEN_CONTAINER = "main_screen_container"
}
/**
 * Main entry point of the application. Sets up the theme and calls [Agendapp] to initialize
 * navigation.
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    EmployeeRepositoryProvider.init(
        EmployeeRepositoryFirebase(
            db = FirebaseFirestore.getInstance(), authRepository = AuthRepositoryFirebase()))
    setContent {
      SampleAppTheme {
        Surface(
            modifier =
                Modifier.fillMaxSize().semantics {
                  testTag = MainActivityTestTags.MAIN_SCREEN_CONTAINER
                },
            color = MaterialTheme.colorScheme.background) {
              Agendapp()
            }
      }
    }
  }
}

/**
 * Root composable containing the navigation graph for the application. This function defines all
 * available routes and how composables are connected.
 */
@Composable
fun Agendapp(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  NavHost(
      navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        navigation(startDestination = Screen.AddEvent.route, route = "Add Event") {
          composable(Screen.AddEvent.route) {
            AddEventScreen(
                onFinish = { navigationActions.navigateTo(Screen.Calendar) },
                onCancel = { navigationActions.navigateBack() })
          }
        }
        navigation(startDestination = Screen.Settings.route, route = "Settings") {
          composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navigationActions.navigateBack() },
                onNavigateToProfile = { navigationActions.navigateTo(Screen.Profile) })
          }
          composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navigationActions.navigateBack() },
                onNavigateToAdminContact = { navigationActions.navigateTo(Screen.AdminContact) })
          }
          composable(Screen.AdminContact.route) {
            AdminContactScreen(onNavigateBack = { navigationActions.navigateBack() })
          }
        }
        navigation(startDestination = Screen.Home.route, route = "Home") {
          composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEdit = { eventId -> navigationActions.navigateToEditEvent(eventId) },
                onNavigateToCalendar = { navigationActions.navigateTo(Screen.Calendar) },
                onNavigateToSettings = { navigationActions.navigateTo(Screen.Settings) },
                onNavigateToMap = { navigationActions.navigateTo(Screen.Map) },
                onNavigateToReplacement = {
                  navigationActions.navigateTo(Screen.ReplacementOverview)
                })
          }
        }
        navigation(startDestination = Screen.Calendar.route, route = "Calendar") {
          composable(Screen.Calendar.route) {
            CalendarScreen(onCreateEvent = { navigationActions.navigateTo(Screen.AddEvent) })
          }
        }
        composable(Screen.ReplacementOverview.route) {
          ReplacementScreen(
              onWaitingConfirmationClick = {
                navigationActions.navigateTo(Screen.ReplacementPending)
              })
        }

        composable(Screen.ReplacementPending.route) { ReplacementPendingListScreen() }
        navigation(startDestination = Screen.Map.route, route = "Map") {
          composable(Screen.Map.route) {
            MapScreen(onGoBack = { navigationActions.navigateBack() })
          }
        }
      }
}
