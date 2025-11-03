package com.android.sample.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.android.sample.Agendapp
import com.android.sample.ui.calendar.CalendarContainer
import com.android.sample.ui.calendar.CalendarGridContent
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultSwipeThreshold
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Helper to perform a horizontal swipe on the calendar event grid.
  private fun swipeEventGrid(deltaX: Float) {
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.EVENT_GRID)
        .assertIsDisplayed()
        .performTouchInput {
          down(center)
          moveBy(Offset(deltaX, 0f))
          up()
        }
  }

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

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCROLL_AREA).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCREEN_ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.SCREEN_ROOT).assertIsDisplayed()
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

  @Test
  fun calendarGridContent_whenSwipeLeft_showsNextWeekWithEvents() {
    // for now : CalendarScreen receive mockEvents
    // the test will check if these events appears on the right week
    composeTestRule.setContent { CalendarScreen() }

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()

    // swipe left of twice the threshold to trigger onSwipeLeft
    swipeEventGrid(-2 * DefaultSwipeThreshold)

    // next week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeRight_showsPreviousWeekWithEvents() {
    // for now : CalendarScreen receive mockEvents
    // the test will check if these events appears on the right week
    composeTestRule.setContent { CalendarScreen() }

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // swipe right of twice the threshold to trigger onSwipeRight
    swipeEventGrid(2 * DefaultSwipeThreshold)

    // previous week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeRightThenLeft_showsCurrentWeekWithEvents() {
    // for now : CalendarScreen receive mockEvents
    // the test will check if these events appears on the right week
    composeTestRule.setContent { CalendarScreen() }

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()

    // swipe right of twice the threshold to trigger onSwipeRight
    swipeEventGrid(2 * DefaultSwipeThreshold)

    // swipe left of twice the threshold to trigger onSwipeLeft
    swipeEventGrid(-2 * DefaultSwipeThreshold)

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_whenSwipeLeftThenRight_showsCurrentWeekWithEvents() {
    // for now : CalendarScreen receive mockEvents
    // the test will check if these events appears on the right week
    composeTestRule.setContent { CalendarScreen() }

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()

    // swipe left of twice the threshold to trigger onSwipeLeft
    swipeEventGrid(-2 * DefaultSwipeThreshold)

    // swipe right of twice the threshold to trigger onSwipeRight
    swipeEventGrid(2 * DefaultSwipeThreshold)

    // current week, should be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Nice Event")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Top Event")
        .assertIsDisplayed()

    // previous week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Previous Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Earlier Event")
        .assertIsNotDisplayed()

    // next week, should not be displayed
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Event")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Later Event")
        .assertIsNotDisplayed()
  }
}
