package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.components.DayHeaderRow
import org.junit.Rule
import org.junit.Test

class CalendarDayHeaderTest {
  @get:Rule val compose = createComposeRule()

  @Test
  fun dayHeaderRow_isDisplayed() {
    compose.setContent { DayHeaderRow() }
    compose.onNodeWithTag(CalendarScreenTestTags.DAY_ROW).assertIsDisplayed()
  }
}
