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
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.screens.EditEventScreen
import com.android.sample.ui.screens.HomeScreen
import com.android.sample.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleAppTheme {
        val navController = rememberNavController()
        val navigationActions = NavigationActions(navController)

        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              Agendapp(navigationActions)
            }
      }
    }
  }
}

@Composable
fun Agendapp(navigationActions: NavigationActions) {
  val navController = navigationActions.navController
  NavHost(navController = navController, startDestination = "home") {
    composable("home") {
      HomeScreen(onNavigateToEdit = { navigationActions.navigateToEdit("E001") })
    }
    composable(
        route = "edit_event/{eventId}",
        arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
          val eventId = it.arguments?.getString("eventId") ?: ""
          EditEventScreen(eventId = eventId, onNavigateBack = { navigationActions.navigateBack() })
        }
  }
}
