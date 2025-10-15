package com.android.sample.ui.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.android.sample.Agendapp_Navigation
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for [Agendapp_Navigation] navigation. This test checks the navigation flow
 * between Home, EditEvent, Calendar, and Settings screens.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AgendappNavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navController: TestNavHostController

  @Before
  fun setupAppNavHost() {
    composeTestRule.setContent {
      navController =
          TestNavHostController(androidx.compose.ui.platform.LocalContext.current).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
          }
      Agendapp_Navigation()
    }
  }

  @Test
  fun navigate_fromHome_toCalendarScreen() {
    // Go to Calendar
    composeTestRule.onNodeWithText("Go to Calendar").assertExists().performClick()

    // Validate screen content
    composeTestRule.onNodeWithText("Calendar").assertExists()
  }

  @Test
  fun navigate_fromHome_toSettingsScreen_andBack() {
    // Go to Settings
    composeTestRule.onNodeWithText("Go to Settings").assertExists().performClick()
    composeTestRule.onNodeWithText("Settings Screen").assertExists()

    // Back to Home
    composeTestRule.onNodeWithText("Back").assertExists().performClick()
    composeTestRule.onNodeWithText("Go to Settings").assertExists()
  }
}
