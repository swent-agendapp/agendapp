package com.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.EmployeeRepositoryFirebase
import com.android.sample.model.organization.EmployeeRepositoryProvider
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.calendar.addEvent.AddEventScreen
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
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
import com.github.se.bootcamp.ui.authentication.SignInScreen
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
 * available routes and how composable are connected.
 */
@Composable
fun Agendapp(
    modifier: Modifier = Modifier,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val addEventViewModel: AddEventViewModel = viewModel()

  val authRepository = AuthRepositoryProvider.repository

  val startDestination =
      if (authRepository.getCurrentUser() != null) Screen.Home.route
      else Screen.Authentication.route

  // Routes and navigation logic
  NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

    // Authentication Screen
    composable(Screen.Authentication.route) {
      SignInScreen(
          credentialManager = credentialManager,
          onSignedIn = { navigationActions.navigateTo(Screen.Home) })
    }

    // Home Screen
    composable(Screen.Home.route) {
      HomeScreen(
          onNavigateToEdit = { eventId -> navigationActions.navigateToEditEvent(eventId) },
          onNavigateToCalendar = { navigationActions.navigateTo(Screen.Calendar) },
          onNavigateToSettings = { navigationActions.navigateTo(Screen.Settings) },
          onNavigateToMap = { navigationActions.navigateTo(Screen.Map) },
          onNavigateToReplacement = { navigationActions.navigateTo(Screen.ReplacementOverview) })
    }

    // Calendar Graph
    composable(Screen.Calendar.route) {
      CalendarScreen(onCreateEvent = { navigationActions.navigateTo(Screen.AddEvent) })
    }

    // Add Event Screen Flow
    navigation(startDestination = Screen.AddEvent.route, route = "Add Event") {
      composable(Screen.AddEvent.route) {
        AddEventScreen(
            onFinish = { navigationActions.navigateTo(Screen.Calendar) },
            onCancel = { navigationActions.navigateBack() })
      }
    }

    // Replacement Overview Screen
    composable(Screen.ReplacementOverview.route) {
      ReplacementScreen(
          onWaitingConfirmationClick = { navigationActions.navigateTo(Screen.ReplacementPending) })
    }

    // Pending Replacement Screen
    composable(Screen.ReplacementPending.route) { ReplacementPendingListScreen() }

    // Settings Graph
    navigation(startDestination = Screen.Settings.route, route = Screen.Settings.name) {
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

    // Map Screen
    composable(Screen.Map.route) { MapScreen(onGoBack = { navigationActions.navigateBack() }) }
  }
}
