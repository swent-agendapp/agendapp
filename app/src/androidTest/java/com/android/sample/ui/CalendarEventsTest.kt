package com.android.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.CalendarGridContent
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.data.TimeSpan
import com.android.sample.ui.calendar.mockData.MockEvent
import java.time.DayOfWeek
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
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.value // Monday = 1 ... Sunday = 7
    val desiredDayOfWeek = DayOfWeek.TUESDAY.value // pick any fixed day

    // Compute this week's Tuesday (or whatever day you want)
    val eventDate = today.plusDays((desiredDayOfWeek - dayOfWeek).toLong())

    val events =
        listOf(
            MockEvent(
                title = "Test Event",
                date = eventDate,
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Emilien",
                backgroundColor = 0xFFFFB74D.toInt(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Test Event")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_showsEventBlocks_forEveryWorkingDays() {
    val events =
        listOf(
            MockEvent(
                title = "Monday Event",
                date = LocalDate.now().with(DayOfWeek.MONDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Monday guy",
                backgroundColor = 0xFF64B5F6.toInt(),
            ),
            MockEvent(
                title = "Tuesday Event",
                date = LocalDate.now().with(DayOfWeek.TUESDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Tuesday guy",
                backgroundColor = 0xFF64B5F6.toInt(),
            ),
            MockEvent(
                title = "Wednesday Event",
                date = LocalDate.now().with(DayOfWeek.WEDNESDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Wednesday guy",
                backgroundColor = 0xFF64B5F6.toInt(),
            ),
            MockEvent(
                title = "Thursday Event",
                date = LocalDate.now().with(DayOfWeek.THURSDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Thursday guy",
                backgroundColor = 0xFF64B5F6.toInt(),
            ),
            MockEvent(
                title = "Friday Event",
                date = LocalDate.now().with(DayOfWeek.FRIDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Friday guy",
                backgroundColor = 0xFF64B5F6.toInt(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Monday Event").assertIsDisplayed()
    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Tuesday Event").assertIsDisplayed()
    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Wednesday Event")
        .assertIsDisplayed()
    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Thursday Event")
        .assertIsDisplayed()
    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Friday Event").assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_doesNotShowsEventBlocks_whenEventsOutOfDayRange() {
    val events =
        listOf(
            MockEvent(
                title = "Out-of-range (early) Event",
                date = LocalDate.of(2000, 1, 1),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Méline",
                backgroundColor = 0xFF81C784.toInt(),
            ),
            MockEvent(
                title = "Out-of-range (late) Event",
                date = LocalDate.of(2100, 1, 1),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Nathan",
                backgroundColor = 0xFF64B5F6.toInt(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Out-of-range (early) Event")
        .assertIsNotDisplayed()
    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Out-of-range (late) Event")
        .assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_doesNotShowsEventBlocks_whenEventsRightNextToDateRange() {
    val events =
        listOf(
            // the day before the start of the week (Monday - 1 = Sunday)
            MockEvent(
                title = "Last Sunday Event",
                date = LocalDate.now().with(DayOfWeek.MONDAY.minus(1)),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Weifeng",
                backgroundColor = 0xFFFFB74D.toInt(),
            ),
            // the day after the working days (Friday + 1 = Saturday)
            MockEvent(
                title = "Next Saturday Event",
                date = LocalDate.now().with(DayOfWeek.FRIDAY.plus(1)),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(1)),
                assigneeName = "Haobin",
                backgroundColor = 0xFFBA68C8.toInt(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Last Sunday Event")
        .assertIsNotDisplayed()
    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Next Saturday Event")
        .assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_doesShowsEventBlocks_whenEventsSemiOverlap() {
    val events =
        listOf(
            // Event on Monday [9:00 - 11:00]
            MockEvent(
                title = "First semi-overlapping Event",
                date = LocalDate.now().with(DayOfWeek.MONDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(2)),
                assigneeName = "Timaël",
                backgroundColor = 0xFF81C784.toInt(),
            ),
            // Event on Monday [10:00 - 12:00]
            MockEvent(
                title = "Second semi-overlapping Event",
                date = LocalDate.now().with(DayOfWeek.MONDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(10, 0), duration = Duration.ofHours(2)),
                assigneeName = "Noa",
                backgroundColor = 0xFFE57373.toInt(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First semi-overlapping Event")
        .assertIsDisplayed()
    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Second semi-overlapping Event")
        .assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_doesShowsEventBlocks_whenEventsFullOverlap() {
    val events =
        listOf(
            // Event on Monday [9:00 - 11:00]
            MockEvent(
                title = "First full-overlapping Event",
                date = LocalDate.now().with(DayOfWeek.MONDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(2)),
                assigneeName = "Timaël",
                backgroundColor = 0xFF81C784.toInt(),
            ),
            // Event on Monday [9:00 - 11:00]
            MockEvent(
                title = "Second full-overlapping Event",
                date = LocalDate.now().with(DayOfWeek.MONDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(9, 0), duration = Duration.ofHours(2)),
                assigneeName = "Noa",
                backgroundColor = 0xFFE57373.toInt(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_First full-overlapping Event")
        .assertIsDisplayed()
    compose
        .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Second full-overlapping Event")
        .assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_doesShowsEventBlock_whenEventOutOfCalendarHour() {
    // Default configuration : screen renders from 8:00 to 23:00 (not before, neither after)
    val events =
        listOf(
            // Event on Monday [6:00 - 10:00]
            MockEvent(
                title = "Morning Event",
                date = LocalDate.now().with(DayOfWeek.MONDAY),
                timeSpan = TimeSpan.of(start = LocalTime.of(6, 0), duration = Duration.ofHours(4)),
                assigneeName = "Méline",
                backgroundColor = 0xFF64B5F6.toInt(),
            ),
            // Event on Friday [20:30 - 23:30]
            MockEvent(
                title = "Night Event",
                date = LocalDate.now().with(DayOfWeek.FRIDAY),
                timeSpan =
                    TimeSpan.of(start = LocalTime.of(20, 30), duration = Duration.ofHours(3)),
                assigneeName = "Emilien",
                backgroundColor = 0xFFBA68C8.toInt(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Morning Event").assertIsDisplayed()
    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Night Event").assertIsDisplayed()
  }

    // todo : test for an event from one day 12:00 to the next day at 12:00 (should see two half blocks)
}
