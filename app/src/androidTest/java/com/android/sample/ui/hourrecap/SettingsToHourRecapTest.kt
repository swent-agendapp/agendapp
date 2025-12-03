package com.android.sample.ui.hourrecap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.settings.SettingsScreen
import com.android.sample.ui.settings.SettingsScreenTestTags
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
}
