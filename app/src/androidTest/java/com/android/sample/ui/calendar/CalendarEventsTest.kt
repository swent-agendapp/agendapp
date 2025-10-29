package com.android.sample.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import com.android.sample.model.calendar.createEvent
import java.time.ZoneId
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue

class CalendarEventsTest {
  private fun at(date: LocalDate, time: LocalTime) =
      date.atTime(time).atZone(ZoneId.systemDefault()).toInstant()
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
            // Event on Tuesday [9:00 - 10:00] — simple visible event
            createEvent(
                title = "Test Event",
                startDate = at(eventDate, LocalTime.of(9, 0)),
                endDate = at(eventDate, LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Test Event")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun calendarGridContent_showsEventBlocks_forEveryWorkingDays() {
    val events =
        listOf(
            // Event on Monday [9:00 - 10:00] — baseline per working day
            createEvent(
                title = "Monday Event",
                startDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Tuesday [9:00 - 10:00] — baseline per working day
            createEvent(
                title = "Tuesday Event",
                startDate = at(LocalDate.now().with(DayOfWeek.TUESDAY), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.TUESDAY), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Wednesday [9:00 - 10:00] — baseline per working day
            createEvent(
                title = "Wednesday Event",
                startDate = at(LocalDate.now().with(DayOfWeek.WEDNESDAY), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.WEDNESDAY), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Thursday [9:00 - 10:00] — baseline per working day
            createEvent(
                title = "Thursday Event",
                startDate = at(LocalDate.now().with(DayOfWeek.THURSDAY), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.THURSDAY), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Friday [9:00 - 10:00] — baseline per working day
            createEvent(
                title = "Friday Event",
                startDate = at(LocalDate.now().with(DayOfWeek.FRIDAY), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.FRIDAY), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
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
            // Event on 2000-01-01 [9:00 - 10:00] — outside current date range (early)
            createEvent(
                title = "Out-of-range (early) Event",
                startDate = at(LocalDate.of(2000, 1, 1), LocalTime.of(9, 0)),
                endDate = at(LocalDate.of(2000, 1, 1), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on 2100-01-01 [9:00 - 10:00] — outside current date range (late)
            createEvent(
                title = "Out-of-range (late) Event",
                startDate = at(LocalDate.of(2100, 1, 1), LocalTime.of(9, 0)),
                endDate = at(LocalDate.of(2100, 1, 1), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
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
            // Event on Sunday [9:00 - 10:00] — adjacent day before visible range
            createEvent(
                title = "Last Sunday Event",
                startDate = at(LocalDate.now().with(DayOfWeek.MONDAY.minus(1)), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.MONDAY.minus(1)), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Saturday [9:00 - 10:00] — adjacent day after visible range
            createEvent(
                title = "Next Saturday Event",
                startDate = at(LocalDate.now().with(DayOfWeek.FRIDAY.plus(1)), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.FRIDAY.plus(1)), LocalTime.of(9, 0)).plus(Duration.ofHours(1)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
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
            createEvent(
                title = "First semi-overlapping Event",
                startDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(9, 0)).plus(Duration.ofHours(2)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Monday [10:00 - 12:00] — semi-overlap with previous
            createEvent(
                title = "Second semi-overlapping Event",
                startDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(10, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(10, 0)).plus(Duration.ofHours(2)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
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
            // Event on Monday [9:00 - 11:00] — first of two fully overlapping
            createEvent(
                title = "First full-overlapping Event",
                startDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(9, 0)).plus(Duration.ofHours(2)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Monday [9:00 - 11:00] — second of two fully overlapping
            createEvent(
                title = "Second full-overlapping Event",
                startDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(9, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(9, 0)).plus(Duration.ofHours(2)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
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
            // Event on Monday [6:00 - 10:00] — partially before visible hours (clipped at 08:00)
            createEvent(
                title = "Morning Event",
                startDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(6, 0)),
                endDate = at(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.of(6, 0)).plus(Duration.ofHours(4)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Friday [20:30 - 23:30] — partially after visible hours (clipped at 23:00)
            createEvent(
                title = "Night Event",
                startDate = at(LocalDate.now().with(DayOfWeek.FRIDAY), LocalTime.of(20, 30)),
                endDate = at(LocalDate.now().with(DayOfWeek.FRIDAY), LocalTime.of(20, 30)).plus(Duration.ofHours(3)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

    compose.setContent { CalendarGridContent(events = events) }

    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Morning Event").assertIsDisplayed()
    compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Night Event").assertIsDisplayed()
  }


    @Test
    fun calendarGridContent_doesNotShowEvent_whenZeroDuration() {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        val start = at(monday, LocalTime.of(10, 0))

        val events = listOf(
            // Event on Monday [10:00 - 10:00] — zero-duration should not render
            createEvent(
                title = "Zero Duration Event",
                startDate = start,
                endDate = start, // same instant
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

        compose.setContent { CalendarGridContent(events = events) }

        compose
            .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Zero Duration Event")
            .assertIsNotDisplayed()
    }

    @Test
    fun calendarGridContent_doesNotShowEvent_whenNegativeDuration() {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        val end = at(monday, LocalTime.of(10, 0))
        val start = at(monday, LocalTime.of(11, 0)) // start after end => negative duration

        assertThrows(IllegalArgumentException::class.java) {
            createEvent(
                title = "Negative Duration Event",
                startDate = start,
                endDate = end,
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            )
        }
    }

    @Test
    fun calendarGridContent_showsEvent_whenStartsExactlyAtStartTime() {
        // Default visible window starts at 08:00
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        val events = listOf(
            // Event on Monday [8:00 - 9:00] — starts exactly at visible startTime
            createEvent(
                title = "Starts At StartTime",
                startDate = at(monday, LocalTime.of(8, 0)),
                endDate = at(monday, LocalTime.of(9, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

        compose.setContent { CalendarGridContent(events = events) }

        compose
            .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Starts At StartTime")
            .assertIsDisplayed()
    }

    @Test
    fun calendarGridContent_showsEvent_whenEndsExactlyAtEndTime() {
        // Default visible window ends at 23:00 (exclusive)
        val friday = LocalDate.now().with(DayOfWeek.FRIDAY)
        val events = listOf(
            // Event on Friday [22:00 - 23:00] — ends exactly at visible endTime (exclusive)
            createEvent(
                title = "Ends At EndTime",
                startDate = at(friday, LocalTime.of(22, 0)),
                endDate = at(friday, LocalTime.of(23, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

        compose.setContent { CalendarGridContent(events = events) }

        compose
            .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Ends At EndTime")
            .assertIsDisplayed()
    }

    @Test
    fun calendarGridContent_showsEventSegments_whenMultiDayMoreThanTwoDays() {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        val thursday = LocalDate.now().with(DayOfWeek.THURSDAY)

        val events = listOf(
            // Event Monday→Thursday [Mon 12:00 - Thu 12:00] — multi-day spanning > 2 days
            createEvent(
                title = "Multi-day 3+",
                startDate = at(monday, LocalTime.of(12, 0)),
                endDate = at(thursday, LocalTime.of(12, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

        compose.setContent { CalendarGridContent(events = events) }

        // We expect one segment per affected day column, allow multiple matches and assert presence
        val nodes = compose
            .onAllNodesWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Multi-day 3+")
            .fetchSemanticsNodes()
        assertTrue("Expected at least one visible segment for multi-day event", nodes.isNotEmpty())
    }

    @Test
    fun calendarGridContent_doesNotShowEvents_whenCompletelyOutsideVisibleHours() {
        val wednesday = LocalDate.now().with(DayOfWeek.WEDNESDAY)

        val events = listOf(
            // Entirely before 08:00
            // Event on Wednesday [6:00 - 7:30] — completely outside visible hours (early)
            createEvent(
                title = "Too Early",
                startDate = at(wednesday, LocalTime.of(6, 0)),
                endDate = at(wednesday, LocalTime.of(7, 30)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Entirely after 23:00
            // Event on Wednesday [23:30 - 23:59] — completely outside visible hours (late)
            createEvent(
                title = "Too Late",
                startDate = at(wednesday, LocalTime.of(23, 30)),
                endDate = at(wednesday, LocalTime.of(23, 59)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

        compose.setContent { CalendarGridContent(events = events) }

        compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Too Early").assertIsNotDisplayed()
        compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Too Late").assertIsNotDisplayed()
    }

    @Test
    fun calendarGridContent_showsAllOverlappingEvents_whenSameSlot() {
        val tuesday = LocalDate.now().with(DayOfWeek.TUESDAY)

        val events = listOf(
            // Event on Tuesday [10:00 - 11:00] — overlapping group A
            createEvent(
                title = "Overlap A",
                startDate = at(tuesday, LocalTime.of(10, 0)),
                endDate = at(tuesday, LocalTime.of(11, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Tuesday [10:00 - 11:00] — overlapping group B
            createEvent(
                title = "Overlap B",
                startDate = at(tuesday, LocalTime.of(10, 0)),
                endDate = at(tuesday, LocalTime.of(11, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
            // Event on Tuesday [10:00 - 11:00] — overlapping group C
            createEvent(
                title = "Overlap C",
                startDate = at(tuesday, LocalTime.of(10, 0)),
                endDate = at(tuesday, LocalTime.of(11, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

        compose.setContent { CalendarGridContent(events = events) }

        compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Overlap A").assertIsDisplayed()
        compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Overlap B").assertIsDisplayed()
        compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Overlap C").assertIsDisplayed()
    }

    @Test
    fun calendarGridContent_showsEvent_withVeryLongTitle() {
        val thursday = LocalDate.now().with(DayOfWeek.THURSDAY)

        val longTitle = "Very Very Long Title That Should Still Be Findable In Tests Even If It Wraps Or Ellipsizes In UI"

        val events = listOf(
            // Event on Thursday [14:00 - 15:00] — very long title rendering
            createEvent(
                title = longTitle,
                startDate = at(thursday, LocalTime.of(14, 0)),
                endDate = at(thursday, LocalTime.of(15, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(), // participants volume is a UI concern, not required for lookup
            ),
        )

        compose.setContent { CalendarGridContent(events = events) }

        compose.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_$longTitle").assertIsDisplayed()
    }

    @Test
    fun calendarGridContent_showsEvent_whenSpanningWeekBoundary() {
        // Event starting Sunday night and ending Monday morning should show on Monday column
        val sunday = LocalDate.now().with(DayOfWeek.MONDAY).minusDays(1)
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)

        val events = listOf(
            // Event spanning week boundary [Sun 22:00 - Mon 10:00] — split across weeks
            createEvent(
                title = "Week Boundary Event",
                startDate = at(sunday, LocalTime.of(22, 0)),
                endDate = at(monday, LocalTime.of(10, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet(),
            ),
        )

        compose.setContent { CalendarGridContent(events = events) }

        compose
            .onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Week Boundary Event")
            .assertIsDisplayed()
    }
}
