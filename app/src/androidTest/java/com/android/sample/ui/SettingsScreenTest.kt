package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.localization.LanguageOption
import com.android.sample.ui.settings.LanguageSelectionSectionTestTags
import com.android.sample.ui.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun settingsScreen_displaysAndBackButtonWorks() {
    var backClicked = false
    composeTestRule.setContent {
      SettingsScreen(
          languageOptions = emptyList(),
          selectedLanguageTag = "",
          onLanguageSelected = {},
          onNavigateBack = { backClicked = true },
          onNavigateToProfile = {})
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
          languageOptions = emptyList(),
          selectedLanguageTag = "",
          onLanguageSelected = {},
          onNavigateBack = {},
          onNavigateToProfile = { profileClicked = true })
    }

    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").performClick()

    assert(profileClicked)
  }

  @Test
  fun settingsScreen_displaysLanguageSection() {
    val options =
        listOf(
            LanguageOption(languageTag = "", displayName = "System", isSystemDefault = true),
            LanguageOption(languageTag = "en", displayName = "English"),
        )
    composeTestRule.setContent {
      SettingsScreen(
          languageOptions = options,
          selectedLanguageTag = "en",
          onLanguageSelected = {},
          onNavigateBack = {},
          onNavigateToProfile = {})
    }

    composeTestRule.onNodeWithTag(LanguageSelectionSectionTestTags.ROOT).assertIsDisplayed()
  }
}
