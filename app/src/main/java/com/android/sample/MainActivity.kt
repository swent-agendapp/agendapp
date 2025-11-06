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
import com.android.sample.model.authentification.AuthRepositoryProvider
import com.android.sample.model.organization.EmployeeRepositoryFirebase
import com.android.sample.model.organization.EmployeeRepositoryProvider
import com.android.sample.ui.calendar.AddEventAttendantScreen
import com.android.sample.ui.calendar.AddEventConfirmationScreen
import com.android.sample.ui.calendar.AddEventTimeAndRecurrenceScreen
import com.android.sample.ui.calendar.AddEventTitleAndDescriptionScreen
import com.android.sample.ui.calendar.AddEventViewModel
import com.android.sample.ui.calendar.CalendarScreen
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

    // Authentication Graph
    navigation(startDestination = Screen.Authentication.route, route = Screen.Authentication.name) {
      composable(Screen.Authentication.route) {
        SignInScreen(
            credentialManager = credentialManager,
            onSignedIn = { navigationActions.navigateTo(Screen.Home) })
      }
    }

    // Home Graph
    navigation(startDestination = Screen.Home.route, route = Screen.Home.name) {
      composable(Screen.Home.route) {
        HomeScreen(
            onNavigateToEdit = { eventId -> navigationActions.navigateToEditEvent(eventId) },
            onNavigateToCalendar = { navigationActions.navigateTo(Screen.Calendar) },
            onNavigateToSettings = { navigationActions.navigateTo(Screen.Settings) },
            onNavigateToMap = { navigationActions.navigateTo(Screen.Map) },
            onNavigateToReplacement = { navigationActions.navigateTo(Screen.ReplacementOverview) })
      }
    }

    // Calendar Graph
    navigation(startDestination = Screen.Calendar.route, route = Screen.Calendar.name) {
      composable(Screen.Calendar.route) {
        CalendarScreen(onCreateEvent = { navigationActions.navigateTo(Screen.AddEventTitle) })
      }
    }

    // Add Event Screen Flow
    navigation(startDestination = Screen.AddEventTitle.route, route = Screen.AddEventTitle.name) {
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
        AddEventConfirmationScreen(
            onFinish = {
              navigationActions.navigateTo(Screen.Calendar)
              addEventViewModel.resetUiState()
            })
      }
    }

    // Replacement Graph
    navigation(
        startDestination = Screen.ReplacementOverview.route,
        route = Screen.ReplacementOverview.name) {
          composable(Screen.ReplacementOverview.route) { ReplacementScreen() }
        }

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

    // Map Graph
    navigation(startDestination = Screen.Map.route, route = Screen.Map.name) {
      composable(Screen.Map.route) { MapScreen(onGoBack = { navigationActions.navigateBack() }) }
    }
  }
}
