package com.android.sample.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.Agendapp
import com.android.sample.ui.calendar.CalendarContainer
import com.android.sample.ui.calendar.CalendarGridContent
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
    // todo : check the testTag of EVENT_GRID, TIME_AXIS_COLUMN and DAY_ROW
  }

  @Test
  fun topTitleIsCorrectlySet() {
    composeTestRule.setContent { CalendarScreen() }

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCROLL_AREA).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCREEN_ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCREEN_ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertTextEquals("Calendar")
  }

  @Test
  fun agendappCorrectlySet() {
    composeTestRule.setContent { Agendapp() }

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertTextEquals("Calendar")
  }

  @Test
  fun calendarContainerComposesWithoutCrash() {
    composeTestRule.setContent { CalendarContainer() }
  }

  @Test
  fun calendarGridContentComposesWithoutCrash() {
    composeTestRule.setContent { CalendarGridContent(modifier = Modifier) }
  }

  @Test
  fun calendarContainerComposes() {
    composeTestRule.setContent { CalendarContainer() }
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()
  }
}
