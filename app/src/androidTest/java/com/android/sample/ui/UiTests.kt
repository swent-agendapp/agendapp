package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.ui.screens.EditEventScreen
import org.junit.Rule
import org.junit.Test

/** Basic UI tests to verify composable rendering and interactions. */
class UiTests {

  @get:Rule val composeTestRule = createComposeRule()

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
