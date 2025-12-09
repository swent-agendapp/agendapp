package com.android.sample

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.credentials.CredentialManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.ui.authentication.SignInScreen
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.calendar.addEvent.AddEventScreen
import com.android.sample.ui.calendar.editEvent.EditEventFlow
import com.android.sample.ui.calendar.eventOverview.EventOverviewScreen
import com.android.sample.ui.common.BottomBar
import com.android.sample.ui.common.BottomBarItem
import com.android.sample.ui.common.BottomBarTestTags
import com.android.sample.ui.invitation.InvitationOverviewScreen
import com.android.sample.ui.map.MapScreen
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.Screen
import com.android.sample.ui.organization.AddOrganizationScreen
import com.android.sample.ui.organization.OrganizationListScreen
import com.android.sample.ui.organization.OrganizationOverViewScreen
import com.android.sample.ui.profile.AdminContactScreen
import com.android.sample.ui.profile.ProfileScreen
import com.android.sample.ui.replacement.ReplacementUpcomingListScreen
import com.android.sample.ui.replacement.mainPage.ReplacementEmployeeFlow
import com.android.sample.ui.replacement.organize.ReplacementOrganizeScreen
import com.android.sample.ui.replacement.route.ProcessReplacementRoute
import com.android.sample.ui.replacement.route.ReplacementPendingRoute
import com.android.sample.ui.settings.SettingsScreen
import com.android.sample.ui.theme.SampleAppTheme

object MainActivityTestTags {
  const val MAIN_SCREEN_CONTAINER = "main_screen_container"
}
/**
 * Main entry point of the application. Sets up the theme and calls [Agendapp] to initialize
 * navigation.
 */
class MainActivity : ComponentActivity() {

  private val locationPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Request location permission when app starts
    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

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

  val authRepository = AuthRepositoryProvider.repository

  val configuration = LocalConfiguration.current
  val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

  val startDestination =
      if (authRepository.getCurrentUser() != null) Screen.SelectOrganization.route
      else Screen.Authentication.route

  val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

  // List of screens that have a bottom navigation bar
  val bottomBarScreens =
      listOf(Screen.Calendar.route, Screen.ReplacementOverview.route, Screen.Settings.route)

  val bottomBarItems =
      listOf(
          BottomBarItem(
              icon = Icons.Default.Accessibility,
              label = "Replacement",
              route = Screen.ReplacementOverview.route,
              onClick = { navigationActions.navigateTo(Screen.ReplacementOverview) },
              contentDescription = "Replacement",
              isSelected = currentRoute == Screen.ReplacementOverview.route,
              testTag = BottomBarTestTags.ITEM_REPLACEMENT),
          BottomBarItem(
              icon = Icons.Default.Event,
              label = "Calendar",
              route = Screen.Calendar.route,
              onClick = { navigationActions.navigateTo(Screen.Calendar) },
              contentDescription = "Calendar",
              isSelected = currentRoute == Screen.Calendar.route,
              testTag = BottomBarTestTags.ITEM_CALENDAR),
          BottomBarItem(
              icon = Icons.Default.Settings,
              label = "Settings",
              route = Screen.Settings.route,
              onClick = { navigationActions.navigateTo(Screen.Settings) },
              contentDescription = "Settings",
              isSelected = currentRoute == Screen.Settings.route,
              testTag = BottomBarTestTags.ITEM_SETTINGS))

  Scaffold(
      bottomBar = {
        AppBottomBar(
            isPortrait = isPortrait,
            currentRoute = currentRoute,
            bottomBarScreens = bottomBarScreens,
            bottomBarItems = bottomBarItems)
      }) { innerPadding ->
        // Routes and navigation logic
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)) {
              authenticationGraph(navigationActions, credentialManager)
              organizationsGraph(navigationActions)
              calendarGraph(navigationActions)
              editEventGraph(navigationActions)
              addEventGraph(navigationActions)
              replacementGraph(navigationActions, credentialManager)
              invitationGraph(navigationActions)
            }
      }
}

@Composable
private fun AppBottomBar(
    isPortrait: Boolean,
    currentRoute: String?,
    bottomBarScreens: List<String>,
    bottomBarItems: List<BottomBarItem>
) {
  if (isPortrait && currentRoute in bottomBarScreens) {
    BottomBar(items = bottomBarItems.map { it.copy(isSelected = it.route == currentRoute) })
  }
}

private fun NavGraphBuilder.authenticationGraph(
    navigationActions: NavigationActions,
    credentialManager: CredentialManager
) {
  // Authentication Screen
  composable(Screen.Authentication.route) {
    SignInScreen(
        credentialManager = credentialManager,
        onSignedIn = { navigationActions.navigateTo(Screen.SelectOrganization) })
  }
}

private fun NavGraphBuilder.organizationsGraph(navigationActions: NavigationActions) {
  // Organization Selection Graph
  navigation(
      startDestination = Screen.SelectOrganization.route, route = Screen.SelectOrganization.name) {
        // Organization List Screen
        composable(Screen.SelectOrganization.route) {
          OrganizationListScreen(
              onOrganizationSelected = { navigationActions.navigateTo(Screen.Calendar) },
              onAddOrganizationClicked = { navigationActions.navigateTo(Screen.AddOrganization) })
        }

        // Add Organization Screen
        composable(Screen.AddOrganization.route) {
          AddOrganizationScreen(
              onNavigateBack = { navigationActions.navigateBack() },
              onFinish = { navigationActions.navigateTo(Screen.SelectOrganization) })
        }
      }
}

private fun NavGraphBuilder.calendarGraph(navigationActions: NavigationActions) {
  // Calendar Graph
  navigation(startDestination = Screen.Calendar.route, route = Screen.Calendar.name) {
    // Main Calendar Screen
    composable(Screen.Calendar.route) {
      CalendarScreen(
          onCreateEvent = { navigationActions.navigateTo(Screen.AddEvent) },
          onEventClick = { event -> navigationActions.navigateToEventOverview(event.id) })
    }
    // Event Overview
    composable(Screen.EventOverview.route) { navBackStackEntry ->
      // Get the Event id from the arguments
      val eventId = navBackStackEntry.arguments?.getString("eventId")

      // Create the Overview screen with the Event id
      eventId?.let {
        EventOverviewScreen(
            eventId = eventId,
            onBackClick = { navigationActions.navigateBack() },
            onEditClick = { id -> navigationActions.navigateToEditEvent(id) },
            onDeleteClick = { navigationActions.navigateBack() })
      } ?: run { Log.e("EventOverviewScreen", "Event id is null") }
    }
  }
}

private fun NavGraphBuilder.editEventGraph(navigationActions: NavigationActions) {
  // Edit Event Graph
  navigation(startDestination = Screen.EditEvent.route, route = Screen.EditEvent.name) {
    composable(Screen.EditEvent.route) { navBackStackEntry ->
      val eventId = navBackStackEntry.arguments?.getString("eventId")
      eventId?.let {
        EditEventFlow(
            eventId = it,
            onCancel = { navigationActions.navigateBack() },
            onFinish = { navigationActions.navigateBack() })
      } ?: run { Log.e("EditEventScreen", "Event id is null") }
    }
  }
}

private fun NavGraphBuilder.addEventGraph(navigationActions: NavigationActions) {
  // Add Event Screen Flow
  navigation(startDestination = Screen.AddEvent.route, route = "Add Event") {
    composable(Screen.AddEvent.route) {
      AddEventScreen(
          onFinish = { navigationActions.navigateTo(Screen.Calendar) },
          onCancel = { navigationActions.navigateBack() })
    }
  }
}

private fun NavGraphBuilder.replacementGraph(
    navigationActions: NavigationActions,
    credentialManager: CredentialManager
) {
  // Replacement Overview Screen
  navigation(
      startDestination = Screen.ReplacementOverview.route,
      route = Screen.ReplacementOverview.name) {
        composable(Screen.ReplacementOverview.route) {
          ReplacementEmployeeFlow(
              onOrganizeClick = { navigationActions.navigateTo(Screen.ReplacementOrganize) },
              onWaitingConfirmationClick = {
                navigationActions.navigateTo(Screen.ReplacementPending)
              },
              onConfirmedClick = { navigationActions.navigateTo(Screen.ReplacementUpcoming) },
              onBack = { navigationActions.navigateTo(Screen.Calendar) },
          )
        }
        composable(Screen.ReplacementOrganize.route) {
          ReplacementOrganizeScreen(
              onCancel = { navigationActions.navigateBack() },
              onProcessNow = { replacement ->
                navigationActions.navigateToReplacementProcess(replacement.id)
              },
              onProcessLater = { navigationActions.navigateTo(Screen.ReplacementOverview) },
          )
        }
        // Pending Replacement Screen
        composable(Screen.ReplacementPending.route) {
          ReplacementPendingRoute(
              onProcessReplacement = { replacement ->
                navigationActions.navigateToReplacementProcess(replacement.id)
              },
              onBack = { navigationActions.navigateBack() },
          )
        }

        // accepted replacement screen
        composable(Screen.ReplacementUpcoming.route) {
          ReplacementUpcomingListScreen(onBack = { navigationActions.navigateBack() })
        }
        composable(Screen.ReplacementProcess.route) { navBackStackEntry ->
          val replacementId = navBackStackEntry.arguments?.getString("replacementId")

          if (replacementId == null) {
            Log.e("ProcessReplacementScreen", "replacementId is null")
            navigationActions.navigateBack()
          } else {
            ProcessReplacementRoute(
                replacementId = replacementId,
                onFinished = {
                  navigationActions.navigateBack()
                  navigationActions.navigateBack()
                  navigationActions.navigateTo(Screen.ReplacementPending)
                },
                onBack = { navigationActions.navigateBack() },
            )
          }
        }
        // Settings Graph
        settingsGraph(navigationActions, credentialManager)
      }
}

private fun NavGraphBuilder.settingsGraph(
    navigationActions: NavigationActions,
    credentialManager: CredentialManager
) {
  // Settings Graph
  navigation(startDestination = Screen.Settings.route, route = Screen.Settings.name) {
    // Settings Screen
    composable(Screen.Settings.route) {
      SettingsScreen(
          onNavigateToUserProfile = { navigationActions.navigateTo(Screen.Profile) },
          onNavigateToAdminInfo = { navigationActions.navigateTo(Screen.AdminContact) },
          onNavigateToMapSettings = { navigationActions.navigateTo(Screen.Map) },
          onNavigateToOrganizationList = {
            navigationActions.navigateTo(Screen.OrganizationOverview)
          })
    }
    // User profile Screen
    composable(Screen.Profile.route) {
      ProfileScreen(
          onNavigateBack = { navigationActions.navigateBack() },
          credentialManager = credentialManager,
          onSignOut = { navigationActions.navigateTo(Screen.Authentication) })
    }
    // Admin contact Screen
    composable(Screen.AdminContact.route) {
      AdminContactScreen(onNavigateBack = { navigationActions.navigateBack() })
    }

    // Map Settings Screen
    composable(Screen.Map.route) { MapScreen(onGoBack = { navigationActions.navigateBack() }) }

    // Organization Overview Screen
    composable(Screen.OrganizationOverview.route) {
      OrganizationOverViewScreen(
          onNavigateBack = { navigationActions.navigateBack() },
          onChangeOrganization = { navigationActions.navigateTo(Screen.ChangeOrganization) },
          onDeleteOrganization = { navigationActions.navigateTo(Screen.SelectOrganization) },
          onInvitationClick = { navigationActions.navigateTo(Screen.InvitationOverview) })
    }
    composable(Screen.ChangeOrganization.route) {
      OrganizationListScreen(
          onOrganizationSelected = { navigationActions.navigateTo(Screen.Settings) },
          onAddOrganizationClicked = { navigationActions.navigateTo(Screen.AddOrganization) })
    }
  }
}

private fun NavGraphBuilder.invitationGraph(navigationActions: NavigationActions) {
  navigation(
      startDestination = Screen.InvitationOverview.route, route = Screen.InvitationOverview.name) {
        composable(route = Screen.InvitationOverview.route) {
          InvitationOverviewScreen(onBack = { navigationActions.navigateBack() })
        }
      }
}
