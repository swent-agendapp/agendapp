package com.android.sample.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.createEvent
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val POSITION_TOLERANCE = 1f

/**
 * Base class exposing shared helpers and setup for the Calendar *events* tests only.
 *
 * Rationale: mirror the structure used in `CalendarScreenTest` so individual suites are small and
 * focused (visibility, overlap, scrolling, boundaries, validation) while reusing helpers.
 */
abstract class BaseEventsTest : RequiresSelectedOrganizationTestBase {

  @get:Rule val compose = createComposeRule()

  override val organizationId = "orgTest"

  protected lateinit var monday: LocalDate

  @Before
  fun setUp() {
    monday = LocalDate.now().with(DayOfWeek.MONDAY)

    setSelectedOrganization()
  }

  /** Converts a (LocalDate, LocalTime) to an Instant in the system zone for concise test setup. */
  protected fun at(date: LocalDate, time: LocalTime) =
      date.atTime(time).atZone(ZoneId.systemDefault()).toInstant()

  /**
   * Returns true if the node with [tag] intersects the root viewport (no assertions/exceptions).
   */
  protected fun isInViewport(tag: String): Boolean {
    val node = compose.onNodeWithTag(tag).fetchSemanticsNode()
    val root = compose.onRoot().fetchSemanticsNode()
    val nb = node.boundsInRoot
    val rb = root.boundsInRoot
    val horizontally = nb.right > rb.left && nb.left < rb.right
    val vertically = nb.bottom > rb.top && nb.top < rb.bottom
    return horizontally && vertically
  }

  /**
   * Scrolls the vertical grid in a bounded way until the node with [tag] intersects the viewport,
   * then asserts it is displayed.
   */
  protected fun scrollUntilVisible(tag: String, maxSwipesPerDirection: Int = 1) {
    // For now, one swipe is enough to see the whole screen, we can increase it when zooming makes
    // grid larger
    // Fast path: already visible
    if (isInViewport(tag)) {
      compose.onNodeWithTag(tag).assertIsDisplayed()
      return
    }

    // Sweep towards later hours first (swipe up). Check after each swipe.
    repeat(maxSwipesPerDirection) {
      compose.onNodeWithTag(CalendarScreenTestTags.SCROLL_AREA).performTouchInput { swipeUp() }
      if (isInViewport(tag)) {
        compose.onNodeWithTag(tag).assertIsDisplayed()
        return
      }
    }

    // Then sweep towards earlier hours (swipe down). Check after each swipe.
    repeat(maxSwipesPerDirection) {
      compose.onNodeWithTag(CalendarScreenTestTags.SCROLL_AREA).performTouchInput { swipeDown() }
      if (isInViewport(tag)) {
        compose.onNodeWithTag(tag).assertIsDisplayed()
        return
      }
    }

    // Final assertion: if the node never entered the viewport, fail clearly here.
    compose.onNodeWithTag(tag).assertIsDisplayed()
  }

  /** Builds the semantics tag for an event block from its [title]. */
  protected fun eventTag(title: String) = "${CalendarScreenTestTags.EVENT_BLOCK}_$title"

  /**
   * Helper to create an [Event] with a simple API.
   *
   * @param title Event title
   * @param date Day of event
   * @param start LocalTime start
   * @param duration Duration of the event
   */
  protected fun ev(title: String, date: LocalDate, start: LocalTime, duration: Duration): Event =
      createEvent(
          organizationId = organizationId,
          title = title,
          startDate = at(date, start),
          endDate = at(date, start).plus(duration),
          cloudStorageStatuses = emptySet(),
          participants = emptySet(),
      )[0]
}

/** Sanity/basic composition for the grid container. */
class EventsSanityTests : BaseEventsTest() {

  @Test
  fun calendarGridContentDisplayed() {
    compose.setContent { CalendarGridContent() }
    compose.onNodeWithTag(CalendarScreenTestTags.EVENT_GRID).assertExists().assertIsDisplayed()
  }
}

/** Visibility-focused tests: which events exist/are visible vs absent in the current week. */
class EventsVisibilityTests : BaseEventsTest() {

  @Test
  fun calendarGridContent_showsEventBlocks_whenEventsProvided() {
    val today = LocalDate.now()
    val eventDate = today.with(DayOfWeek.TUESDAY)

    val events = listOf(ev("Test Event", eventDate, LocalTime.of(9, 0), Duration.ofHours(1)))

    compose.setContent { CalendarGridContent(events = events) }

    val tag = eventTag("Test Event")
    compose.onNodeWithTag(tag).assertExists()
    scrollUntilVisible(tag)
  }

  @Test
  fun calendarGridContent_showsEventBlocks_forEveryDayOfWeek() {
    val events =
        listOf(
            // Event on Monday [9:00 - 10:00]
            ev(
                "Monday Event",
                LocalDate.now().with(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
            // Event on Tuesday [9:00 - 10:00]
            ev(
                "Tuesday Event",
                LocalDate.now().with(DayOfWeek.TUESDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
            // Event on Wednesday [9:00 - 10:00]
            ev(
                "Wednesday Event",
                LocalDate.now().with(DayOfWeek.WEDNESDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
            // Event on Thursday [9:00 - 10:00]
            ev(
                "Thursday Event",
                LocalDate.now().with(DayOfWeek.THURSDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
            // Event on Friday [9:00 - 10:00]
            ev(
                "Friday Event",
                LocalDate.now().with(DayOfWeek.FRIDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
            // Event on Saturday [9:00 - 10:00]
            ev(
                "Saturday Event",
                LocalDate.now().with(DayOfWeek.SATURDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
            // Event on Sunday [9:00 - 10:00]
            ev(
                "Sunday Event",
                LocalDate.now().with(DayOfWeek.SUNDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    scrollUntilVisible(eventTag("Monday Event"))
    scrollUntilVisible(eventTag("Tuesday Event"))
    scrollUntilVisible(eventTag("Wednesday Event"))
    scrollUntilVisible(eventTag("Thursday Event"))
    scrollUntilVisible(eventTag("Friday Event"))
    scrollUntilVisible(eventTag("Saturday Event"))
    scrollUntilVisible(eventTag("Sunday Event"))
  }

  @Test
  fun calendarGridContent_doesNotShowsEventBlocks_whenEventsOutOfDayRange() {
    val events =
        listOf(
            // Event on 2000-01-01 [9:00 - 10:00] — outside current date range (early)
            ev(
                "Out-of-range (early) Event",
                LocalDate.of(2000, 1, 1),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
            // Event on 2100-01-01 [9:00 - 10:00] — outside current date range (late)
            ev(
                "Out-of-range (late) Event",
                LocalDate.of(2100, 1, 1),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    // Use assertDoesNotExist(): nodes are not expected to be composed at all.
    compose.onNodeWithTag(eventTag("Out-of-range (early) Event")).assertDoesNotExist()
    compose.onNodeWithTag(eventTag("Out-of-range (late) Event")).assertDoesNotExist()
  }

  @Test
  fun calendarGridContent_doesNotShowsEventBlocks_whenEventsRightNextToDateRange() {
    val events =
        listOf(
            // Event on previous Sunday [9:00 - 10:00] — adjacent day before visible range
            ev(
                "Previous Sunday Event",
                monday.minusDays(1),
                LocalTime.of(9, 0),
                Duration.ofHours(1)),
            // Event on next Monday [9:00 - 10:00] — adjacent day after visible range
            ev("Next Monday Event", monday.plusDays(7), LocalTime.of(9, 0), Duration.ofHours(1)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    // Use assertDoesNotExist(): nodes should not be present for out-of-range days.
    compose.onNodeWithTag(eventTag("Previous Sunday Event")).assertDoesNotExist()
    compose.onNodeWithTag(eventTag("Next Monday Event")).assertDoesNotExist()
  }
}

/** Scrolling-focused tests. */
class EventsScrollingTests : BaseEventsTest() {

  @Test
  fun calendarGridContent_showsEventBlocks_outsideInitialViewport_afterScroll() {
    // Grid covers 00:00–24:00; events can start before 08:00 or end after 23:00 and still be shown
    // after scrolling.
    val events =
        listOf(
            // Event on Monday [6:00 - 10:00] — partially before visible hours (clipped at 08:00)
            ev(
                "Morning Event",
                LocalDate.now().with(DayOfWeek.MONDAY),
                LocalTime.of(6, 0),
                Duration.ofHours(4)),
            // Event on Friday [20:30 - 23:30] — partially after visible hours (clipped at 23:00)
            ev(
                "Night Event",
                LocalDate.now().with(DayOfWeek.FRIDAY),
                LocalTime.of(20, 30),
                Duration.ofHours(3)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    scrollUntilVisible(eventTag("Morning Event"))
    scrollUntilVisible(eventTag("Night Event"))
  }

  @Test
  fun calendarGridContent_eventsOutsideInitialViewport_becomeVisibleAfterScroll() {
    val wednesday = LocalDate.now().with(DayOfWeek.WEDNESDAY)
    val events =
        listOf(
            ev("Too Early", wednesday, LocalTime.of(6, 0), Duration.ofMinutes(90)),
            ev("Too Late", wednesday, LocalTime.of(23, 30), Duration.ofMinutes(29)),
        )
    compose.setContent { CalendarGridContent(events = events) }

    scrollUntilVisible(eventTag("Too Early"))
    scrollUntilVisible(eventTag("Too Late"))
  }
}

/** Overlap-focused tests (semi-overlap, full-overlap, same slot, multi-day segments). */
class EventsOverlapTests : BaseEventsTest() {

  @Test
  fun calendarGridContent_doesShowsEventBlocks_whenEventsSemiOverlap() {
    val events =
        listOf(
            // Event on Monday [9:00 - 11:00]
            ev(
                "First semi-overlapping Event",
                LocalDate.now().with(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(2)),
            // Event on Monday [10:00 - 12:00] — semi-overlap with previous
            ev(
                "Second semi-overlapping Event",
                LocalDate.now().with(DayOfWeek.MONDAY),
                LocalTime.of(10, 0),
                Duration.ofHours(2)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    scrollUntilVisible(eventTag("First semi-overlapping Event"))
    scrollUntilVisible(eventTag("Second semi-overlapping Event"))
  }

  @Test
  fun calendarGridContent_doesShowsEventBlocks_whenEventsFullOverlap() {
    val events =
        listOf(
            // Event on Monday [9:00 - 11:00] — first of two fully overlapping
            ev(
                "First full-overlapping Event",
                LocalDate.now().with(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(2)),
            // Event on Monday [9:00 - 11:00] — second of two fully overlapping
            ev(
                "Second full-overlapping Event",
                LocalDate.now().with(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                Duration.ofHours(2)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    scrollUntilVisible(eventTag("First full-overlapping Event"))
    scrollUntilVisible(eventTag("Second full-overlapping Event"))
  }

  @Test
  fun calendarGridContent_showsAllOverlappingEvents_whenSameSlot() {
    val tuesday = LocalDate.now().with(DayOfWeek.TUESDAY)

    val events =
        listOf(
            // Event on Tuesday [10:00 - 11:00] — overlapping group A
            ev("Overlap A", tuesday, LocalTime.of(10, 0), Duration.ofHours(1)),
            // Event on Tuesday [10:00 - 11:00] — overlapping group B
            ev("Overlap B", tuesday, LocalTime.of(10, 0), Duration.ofHours(1)),
            // Event on Tuesday [10:00 - 11:00] — overlapping group C
            ev("Overlap C", tuesday, LocalTime.of(10, 0), Duration.ofHours(1)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    scrollUntilVisible(eventTag("Overlap A"))
    scrollUntilVisible(eventTag("Overlap B"))
    scrollUntilVisible(eventTag("Overlap C"))
  }

  @Test
  fun calendarGridContent_showsEventSegments_whenMultiDayMoreThanTwoDays() {
    val monday = LocalDate.now().with(DayOfWeek.MONDAY)
    val thursday = LocalDate.now().with(DayOfWeek.THURSDAY)

    val events =
        // Event Monday→Thursday [Mon 12:00 - Thu 12:00] — multi-day spanning > 2 days
        createEvent(
            organizationId = organizationId,
            title = "Multi-day 3+",
            startDate = at(monday, LocalTime.of(12, 0)),
            endDate = at(thursday, LocalTime.of(12, 0)),
            cloudStorageStatuses = emptySet(),
            participants = emptySet(),
        )

    compose.setContent { CalendarGridContent(events = events) }

    // We expect one segment per affected day column, allow multiple matches and assert presence
    val nodes = compose.onAllNodesWithTag(eventTag("Multi-day 3+")).fetchSemanticsNodes()
    assertTrue("Expected at least one visible segment for multi-day event", nodes.isNotEmpty())
  }

  /**
   * Verifies that two non-overlapping events on the same day are laid out with the same horizontal
   * position and width (i.e., they use the full column width).
   */
  @Test
  fun calendarGridContent_nonOverlappingEvents_shareSameHorizontalPositionAndWidth() {
    val events =
        listOf(
            // Event on Monday [9:00 - 10:00]
            ev("First non-overlapping", monday, LocalTime.of(9, 0), Duration.ofHours(1)),
            // Event on Monday [11:00 - 12:00]
            ev("Second non-overlapping", monday, LocalTime.of(11, 0), Duration.ofHours(1)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    val firstTag = eventTag("First non-overlapping")
    val secondTag = eventTag("Second non-overlapping")

    // Ensure both events are visible in the viewport before checking bounds.
    scrollUntilVisible(firstTag)
    scrollUntilVisible(secondTag)

    val firstBounds = compose.onNodeWithTag(firstTag).fetchSemanticsNode().boundsInRoot
    val secondBounds = compose.onNodeWithTag(secondTag).fetchSemanticsNode().boundsInRoot

    val firstWidth = firstBounds.right - firstBounds.left
    val secondWidth = secondBounds.right - secondBounds.left

    // Same horizontal position (allowing a small tolerance due to rendering).
    assertTrue(kotlin.math.abs(firstBounds.left - secondBounds.left) < POSITION_TOLERANCE)
    assertTrue(kotlin.math.abs(firstBounds.right - secondBounds.right) < POSITION_TOLERANCE)

    // Same width (full column width).
    assertTrue(kotlin.math.abs(firstWidth - secondWidth) < POSITION_TOLERANCE)
  }

  /**
   * Verifies that when two events overlap in time, they are rendered side-by-side with reduced
   * width compared to a non-overlapping event in the same column.
   */
  @Test
  fun calendarGridContent_overlappingEvents_shareColumnWidthAndShiftHorizontally() {
    val events =
        listOf(
            // Solo event on Monday [8:00 - 9:00] — no overlap
            ev("Solo Event", monday, LocalTime.of(8, 0), Duration.ofHours(1)),
            // Overlapping group on Monday [10:00 - 12:00] and [11:00 - 13:00]
            ev("Overlap 1", monday, LocalTime.of(10, 0), Duration.ofHours(2)),
            ev("Overlap 2", monday, LocalTime.of(11, 0), Duration.ofHours(2)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    val soloTag = eventTag("Solo Event")
    val overlap1Tag = eventTag("Overlap 1")
    val overlap2Tag = eventTag("Overlap 2")

    scrollUntilVisible(soloTag)
    scrollUntilVisible(overlap1Tag)
    scrollUntilVisible(overlap2Tag)

    val soloBounds = compose.onNodeWithTag(soloTag).fetchSemanticsNode().boundsInRoot
    val overlap1Bounds = compose.onNodeWithTag(overlap1Tag).fetchSemanticsNode().boundsInRoot
    val overlap2Bounds = compose.onNodeWithTag(overlap2Tag).fetchSemanticsNode().boundsInRoot

    val soloWidth = soloBounds.right - soloBounds.left
    val overlap1Width = overlap1Bounds.right - overlap1Bounds.left
    val overlap2Width = overlap2Bounds.right - overlap2Bounds.left

    // Overlapping events should be narrower than the solo event (they share the column width).
    assertTrue(overlap1Width < soloWidth)
    assertTrue(overlap2Width < soloWidth)

    // Overlapping events should have (approximately) the same width.
    assertTrue(kotlin.math.abs(overlap1Width - overlap2Width) < POSITION_TOLERANCE)

    // They should be placed side-by-side: different left positions, non-overlapping horizontally.
    assertTrue(kotlin.math.abs(overlap1Bounds.left - overlap2Bounds.left) > POSITION_TOLERANCE)
    val leftEvent =
        if (overlap1Bounds.left < overlap2Bounds.left) overlap1Bounds else overlap2Bounds
    val rightEvent =
        if (overlap1Bounds.left < overlap2Bounds.left) overlap2Bounds else overlap1Bounds
    assertTrue(leftEvent.right <= rightEvent.left + POSITION_TOLERANCE)
  }

  /**
   * Verifies that three events in the exact same time slot are rendered as three distinct columns
   * side-by-side within the same day.
   */
  @Test
  fun calendarGridContent_threeEventsSameSlot_areRenderedSideBySide() {
    val tuesday = LocalDate.now().with(DayOfWeek.TUESDAY)
    val events =
        listOf(
            ev("Triple A", tuesday, LocalTime.of(10, 0), Duration.ofHours(1)),
            ev("Triple B", tuesday, LocalTime.of(10, 0), Duration.ofHours(1)),
            ev("Triple C", tuesday, LocalTime.of(10, 0), Duration.ofHours(1)),
        )

    compose.setContent { CalendarGridContent(events = events) }

    val tagA = eventTag("Triple A")
    val tagB = eventTag("Triple B")
    val tagC = eventTag("Triple C")

    scrollUntilVisible(tagA)
    scrollUntilVisible(tagB)
    scrollUntilVisible(tagC)

    val boundsA = compose.onNodeWithTag(tagA).fetchSemanticsNode().boundsInRoot
    val boundsB = compose.onNodeWithTag(tagB).fetchSemanticsNode().boundsInRoot
    val boundsC = compose.onNodeWithTag(tagC).fetchSemanticsNode().boundsInRoot

    val widthA = boundsA.right - boundsA.left
    val widthB = boundsB.right - boundsB.left
    val widthC = boundsC.right - boundsC.left

    // All three should have approximately the same width.
    assertTrue(kotlin.math.abs(widthA - widthB) < POSITION_TOLERANCE)
    assertTrue(kotlin.math.abs(widthB - widthC) < POSITION_TOLERANCE)

    // Their horizontal positions (left) should all be distinct (side-by-side columns).
    val lefts = listOf(boundsA.left, boundsB.left, boundsC.left)
    val distinctLefts = lefts.toSet()
    assertTrue(
        "Expected three distinct horizontal positions for triple overlap", distinctLefts.size == 3)
  }
}

/** Validation/guard-rail tests. */
class EventsValidationTests : BaseEventsTest() {

  @Test
  fun calendarGridContent_doesNotShowEvent_whenZeroDuration() {
    val start = at(monday, LocalTime.of(10, 0))

    val events =
        // Event on Monday [10:00 - 10:00] — zero-duration should not render
        createEvent(
            organizationId = organizationId,
            title = "Zero Duration Event",
            startDate = start,
            endDate = start, // same instant
            cloudStorageStatuses = emptySet(),
            participants = emptySet(),
        )

    compose.setContent { CalendarGridContent(events = events) }

    // Use assertIsNotDisplayed(): a zero-duration event should not be composed.
    compose.onNodeWithTag(eventTag("Zero Duration Event")).assertIsNotDisplayed()
  }

  @Test
  fun calendarGridContent_doesNotShowEvent_whenNegativeDuration() {
    val end = at(monday, LocalTime.of(10, 0))
    val start = at(monday, LocalTime.of(11, 0)) // start after end => negative duration

    assertThrows(IllegalArgumentException::class.java) {
      createEvent(
          organizationId = organizationId,
          title = "Negative Duration Event",
          startDate = start,
          endDate = end,
          cloudStorageStatuses = emptySet(),
          participants = emptySet(),
      )
    }
  }
}

/**
 * Week-boundary focused tests: more descriptive comments to explain expectations.
 *
 * Context: Week view is Monday→Sunday. When an event spans across weeks, only the segment that
 * intersects the visible week should render (e.g., Sun→Mon shows on Sun of current or Mon of next,
 * as appropriate).
 */
class EventsWeekBoundaryTests : BaseEventsTest() {

  @Test
  fun calendarGridContent_showsEvent_whenSpanningFromPreviousSundayToCurrentMonday() {
    // Event starting Sunday (previous week) and ending Monday (current week) should show on Monday
    // column (the portion intersecting the current visible week). This ensures boundary clipping
    // is applied correctly at week transitions.
    val previousSunday = monday.minusDays(1)

    val events =
        createEvent(
            organizationId = organizationId,
            title = "Week Boundary Event",
            startDate = at(previousSunday, LocalTime.of(22, 0)),
            endDate = at(monday, LocalTime.of(10, 0)),
            cloudStorageStatuses = emptySet(),
            participants = emptySet(),
        )

    compose.setContent { CalendarGridContent(events = events) }

    scrollUntilVisible(eventTag("Week Boundary Event"))
  }

  @Test
  fun calendarGridContent_showsEvent_whenSpanningFromCurrentSundayToNextMonday() {
    // Event starting Sunday (current week) and ending Monday (next week) should show on Sunday
    // column (the portion intersecting the current visible week). Again, this verifies that only
    // the in-range segment is rendered for cross-week events.
    val currentSunday = LocalDate.now().with(DayOfWeek.SUNDAY)
    val nextMonday = currentSunday.plusDays(1)

    val events =
        createEvent(
            organizationId = organizationId,
            title = "Current Week Boundary Event",
            startDate = at(currentSunday, LocalTime.of(22, 0)),
            endDate = at(nextMonday, LocalTime.of(10, 0)),
            cloudStorageStatuses = emptySet(),
            participants = emptySet(),
        )

    compose.setContent { CalendarGridContent(events = events) }

    scrollUntilVisible(eventTag("Current Week Boundary Event"))
  }
}
