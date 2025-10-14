package com.android.sample

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.CalendarGridContent
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.data.TimeSpan
import com.android.sample.ui.calendar.mockData.MockEvent
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Rule
import org.junit.Test

class CalendarEventsTest {
  @get:Rule val compose = createComposeRule()

  @Test
  fun calendarGridContentDisplayed() {
    compose.setContent { CalendarGridContent() }
    compose.onNodeWithTag(CalendarScreenTestTags.EVENT_GRID).assertExists().assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_showsEventBlocks_whenEventsProvided() {
    val events =
        listOf(
            MockEvent(
                title = "Test Event",
                date = LocalDate.of(2025, 10, 14),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Emilien",
                backgroundColor = 0xFFFFB74D.toInt()))

    compose.setContent { CalendarGridContent(events = events) }

    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Test Event").assertIsDisplayed()
  }
}
