package com.android.sample.model.replacement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.navigation.Screen
import com.android.sample.ui.replacement.ReplacementOverviewScreen
import com.android.sample.ui.replacement.ReplacementOverviewTestTags
import com.android.sample.ui.replacement.ReplacementPendingListScreen
import com.android.sample.ui.replacement.ReplacementPendingTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReplacementNavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun clickingWaitingCard_navigatesFromOverviewToPending() {
    composeTestRule.setContent {
      val navController = rememberNavController()

      NavHost(navController = navController, startDestination = Screen.ReplacementOverview.route) {
        composable(Screen.ReplacementOverview.route) {
          ReplacementOverviewScreen(
              onOrganizeClick = {},
              onProcessClick = {},
              onWaitingConfirmationClick = {
                navController.navigate(Screen.ReplacementPending.route)
              },
              onConfirmedClick = {})
        }

        composable(Screen.ReplacementPending.route) { ReplacementPendingListScreen() }
      }
    }

    composeTestRule
        .onNodeWithTag(ReplacementOverviewTestTags.CARD_WAITING)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag(ReplacementPendingTestTags.SCREEN).assertIsDisplayed()
  }
}
