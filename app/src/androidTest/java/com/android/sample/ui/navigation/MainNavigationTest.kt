package com.android.sample.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// -------- Fake EditEvent composable --------
@Composable
fun FakeEditEventFlow(eventId: String, onCancel: () -> Unit) {
  Column {
    Text("Editing $eventId")

    Button(onClick = onCancel, modifier = Modifier.testTag("cancelEdit")) { Text("Cancel") }
  }
}

// -------- Test Class --------
@RunWith(AndroidJUnit4::class)
class MainNavigationTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun navigateToEditEventFlow_loadsAndCanNavigateBack() {
    lateinit var navController: NavHostController

    composeRule.setContent {
      navController = rememberNavController()

      NavHost(
          navController = navController,
          startDestination = "test_start",
      ) {
        composable("test_start") {
          Button(
              onClick = { navController.navigate("editEvent/E123") },
              modifier = Modifier.testTag("goToEdit")) {
                Text("Go Edit")
              }
        }

        composable(
            route = "editEvent/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { backStack ->
              val eventId = backStack.arguments?.getString("eventId")
              FakeEditEventFlow(eventId = eventId!!, onCancel = { navController.navigateUp() })
            }
      }
    }

    composeRule.onNodeWithTag("goToEdit").performClick()
    composeRule.onNodeWithText("Editing E123").assertExists()
    composeRule.onNodeWithTag("cancelEdit").performClick()

    composeRule.onNodeWithTag("goToEdit").assertExists()
  }
}
