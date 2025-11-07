package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
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
      SettingsScreen(
          onNavigateBack = { backClicked = true },
          onNavigateToProfile = {},
          onNavigateToLanguageSelection = {})
    }

    composeTestRule.onNodeWithText("Settings Screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Back").performClick()

    assert(backClicked)
  }

  @Test
  fun settingsScreen_profileButtonWorks() {
    var profileClicked = false
    composeTestRule.setContent {
      SettingsScreen(
          onNavigateBack = {},
          onNavigateToProfile = { profileClicked = true },
          onNavigateToLanguageSelection = {})
    }

    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").performClick()

    assert(profileClicked)
  }

  @Test
  fun settingsScreen_languageButtonNavigates() {
    var languageClicked = false
    composeTestRule.setContent {
      SettingsScreen(
          onNavigateBack = {},
          onNavigateToProfile = {},
          onNavigateToLanguageSelection = { languageClicked = true })
    }

    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.SELECT_LANGUAGE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(languageClicked)
  }
}
