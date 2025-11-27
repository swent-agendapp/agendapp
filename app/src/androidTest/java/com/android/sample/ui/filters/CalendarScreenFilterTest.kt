package com.android.sample.ui.filters

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.calendar.CalendarScreen
import com.android.sample.ui.calendar.CalendarScreenTestTags
import org.junit.Rule
import org.junit.Test

class CalendarScreenFilterTest {

  @get:Rule val compose = createComposeRule()

  /**
   * Test to verify that the filter button is displayed and opens the filter bottom sheet when
   * clicked.
   */
  @Test
  fun filterButton_isDisplayedAndOpensBottomSheet() {
    compose.setContent { CalendarScreen() }

    // 1. Filter button exists and is visible
    compose.onNodeWithTag(CalendarScreenTestTags.FILTER_BUTTON).assertExists().assertIsDisplayed()

    // 2. Click filter button
    compose.onNodeWithTag(CalendarScreenTestTags.FILTER_BUTTON).performClick()

    // 3. Verify bottom sheet appears
    compose
        .onNodeWithTag(CalendarScreenTestTags.FILTER_BOTTOM_SHEET)
        .assertExists()
        .assertIsDisplayed()
  }
}
