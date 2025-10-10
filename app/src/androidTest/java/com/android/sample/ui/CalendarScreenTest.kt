package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.calendar.CalendarScreenTestTags
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testTagsAreCorrectlySet() {
    composeTestRule.setContent { CalendarScreen() }

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.EVENT_GRID).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TIME_AXIS_COLUMN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.DAY_ROW).assertIsDisplayed()
  }

  @Test
  fun topTitleIsCorrectlySet() {
    composeTestRule.setContent { CalendarScreen() }

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertTextEquals("Calendar")
  }
}
