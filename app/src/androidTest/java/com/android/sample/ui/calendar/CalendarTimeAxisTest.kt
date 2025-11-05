package com.android.sample.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class CalendarTimeAxisTest {
  @get:Rule val compose = createComposeRule()

  @Test
  fun timeAxis_isDisplayed() {
    compose.setContent { CalendarGridContent() }
    compose
        .onNodeWithTag(CalendarScreenTestTags.TIME_AXIS_COLUMN)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun nowIndicator_isDisplayed() {
    compose.setContent { CalendarGridContent() }
    compose.onNodeWithTag(CalendarScreenTestTags.NOW_INDICATOR).assertExists().assertIsDisplayed()
  }
}
