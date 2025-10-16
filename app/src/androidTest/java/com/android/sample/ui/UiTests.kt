package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.ui.screens.EditEventScreen
import com.android.sample.ui.screens.HomeScreen
import org.junit.Rule
import org.junit.Test

/** Basic UI tests to verify composable rendering and interactions. */
class UiTests {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun homeScreen_displaysAndRespondsToClicks() {
    var editClicked = false
    var calendarClicked = false
    var settingsClicked = false

    composeTestRule.setContent {
      HomeScreen(
          onNavigateToEdit = { editClicked = true },
          onNavigateToCalendar = { calendarClicked = true },
          onNavigateToSettings = { settingsClicked = true })
    }

    composeTestRule.onNodeWithText("Go to Edit Event").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithText("Go to Calendar").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithText("Go to Settings").assertIsDisplayed().performClick()

    assert(editClicked)
    assert(calendarClicked)
    assert(settingsClicked)
  }

  @Test
  fun editEventScreen_showsContent_andBackWorks() {
    var backClicked = false
    composeTestRule.setContent {
      EditEventScreen(eventId = "E123", onNavigateBack = { backClicked = true })
    }
    composeTestRule.onNodeWithText("Editing event ID: E123").assertIsDisplayed()
    composeTestRule.onNodeWithText("Back").performClick()
    assert(backClicked)
  }
}
