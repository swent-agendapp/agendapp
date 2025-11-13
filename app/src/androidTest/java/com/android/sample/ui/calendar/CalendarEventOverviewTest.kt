package com.android.sample.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.calendar.eventOverview.EventOverviewScreen
import com.android.sample.ui.calendar.eventOverview.EventOverviewScreenTestTags
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [EventOverviewScreen].
 *
 * These tests focus on verifying that the screen shows its main structure (top bar and content
 * container) and that the back button correctly triggers the provided callback.
 */
class CalendarEventOverviewTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eventOverview_showsTopBarAndRootScreen() {
    // We set an EventOverviewScreen, composed with any event id
    composeTestRule.setContent { EventOverviewScreen(eventId = "basic-id") }

    // Check if the screen container is displayed (by checking the root)
    composeTestRule.onNodeWithTag(EventOverviewScreenTestTags.SCREEN_ROOT).assertIsDisplayed()

    // Check that the top bar is displayed
    composeTestRule.onNodeWithTag(EventOverviewScreenTestTags.TOP_BAR).assertIsDisplayed()
  }

  @Test
  fun eventOverview_backButton_callsOnBackClick() {
    var backClicked = false

    // We set an EventOverviewScreen with a custom back callback
    composeTestRule.setContent {
      EventOverviewScreen(
          eventId = "dummy-id",
          onBackClick = { backClicked = true },
      )
    }

    // We trigger the back button
    composeTestRule.onNodeWithTag(EventOverviewScreenTestTags.BACK_BUTTON).performClick()

    // Assert that the callback has been executed
    Assert.assertTrue(
        "Back callback should be triggered when clicking the back button", backClicked)
  }
}
