package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.ui.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun settingsScreen_displaysAndBackButtonWorks() {
    var backClicked = false
    composeTestRule.setContent { SettingsScreen(onNavigateBack = { backClicked = true }) }

    composeTestRule.onNodeWithText("Settings Screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Back").performClick()

    assert(backClicked)
  }
}
