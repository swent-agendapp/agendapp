package com.android.sample.ui.hourRecap

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.android.sample.ui.navigation.Screen
import com.android.sample.ui.settings.SettingsScreen
import com.android.sample.ui.settings.SettingsScreenTestTags
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsToHourRecapTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun settingsScreen_displaysAllButtons() {
    composeTestRule.setContent { SettingsScreen() }

    composeTestRule.onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.MAP_SETTINGS_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ORGANIZATION_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.HOURRECAP_BUTTON).assertExists()
  }

  @Test
  fun hourRecapButton_isDisplayed_andClickable() {
    var clicked = false

    composeTestRule.setContent { SettingsScreen(onNavigateToHourRecap = { clicked = true }) }

    // Verify button is displayed
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.HOURRECAP_BUTTON)
        .assertExists()
        .assertIsDisplayed()

    // Perform click
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.HOURRECAP_BUTTON).performClick()

    // Ensure callback was triggered
    assert(clicked)
  }

  /** Test navigation from Settings screen to Hour Recap screen. */
  @Test
  fun settingsHourRecapButton_navigatesToHourRecap() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val navController =
        TestNavHostController(context).apply { navigatorProvider.addNavigator(ComposeNavigator()) }

    composeTestRule.setContent {
      NavHost(navController = navController, startDestination = Screen.Settings.route) {
        composable(Screen.Settings.route) {
          SettingsScreen(
              onNavigateToUserProfile = {},
              onNavigateToAdminInfo = {},
              onNavigateToMapSettings = {},
              onNavigateToHourRecap = { navController.navigate(Screen.HourRecap.route) },
              onNavigateToOrganizationList = {})
        }

        composable(Screen.HourRecap.route) {
          // Empty UI with testTag so we can verify navigation succeeded
          Box(Modifier.testTag(HourRecapTestTags.SCREEN_ROOT))
        }
      }
    }

    // Click the Settings → Hour Recap button
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.HOURRECAP_BUTTON)
        .assertExists()
        .performClick()

    composeTestRule.waitForIdle()
    // Verify that we have navigated to the Hour Recap screen
    assertEquals(Screen.HourRecap.route, navController.currentDestination?.route)
  }
}
