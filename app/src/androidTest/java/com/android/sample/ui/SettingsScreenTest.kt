package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.ui.settings.SettingsScreen
import com.android.sample.ui.settings.SettingsScreenTestTags
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun settingsScreen_displaysAndBackButtonWorks() {
    var backClicked = false
    composeTestRule.setContent {
      SettingsScreen(onNavigateBack = { backClicked = true }, onNavigateToUserProfile = {})
    }

    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ROOT).isDisplayed()
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(backClicked)
  }

  @Test
  fun settingsScreen_profileButtonWorks() {
    var profileClicked = false
    composeTestRule.setContent {
      SettingsScreen(onNavigateBack = {}, onNavigateToUserProfile = { profileClicked = true })
    }

    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").performClick()

    assert(profileClicked)
  }
}
