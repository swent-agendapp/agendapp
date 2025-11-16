package com.android.sample.ui.calendar

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.createEvent
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.calendar.utils.EventOverlapCalculator
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [EventOverlapCalculator].
 *
 * These tests focus on:
 * - Empty input.
 * - Non-overlapping events.
 * - Simple overlapping events.
 * - Events that only touch at their boundaries (no overlap).
 * - Chained overlaps where A overlaps B and B overlaps C, but A and C do not overlap directly.
 */
class EventOverlapCalculatorTest {

  private val baseDate: LocalDate = LocalDate.of(2024, 1, 1)

  /**
   * Helper to create an [Event] on [baseDate] between the given times using [DateTimeUtils] and the
   * real [createEvent] factory.
   *
   * This ensures we rely on the same time conversion utilities as the production code.
   */
  private fun createEventForTimes(
      title: String,
      startHour: Int,
      startMinute: Int,
      endHour: Int,
      endMinute: Int,
  ): Event {
    val startTime = LocalTime.of(startHour, startMinute)
    val endTime = LocalTime.of(endHour, endMinute)
    val startInstant = DateTimeUtils.localDateTimeToInstant(baseDate, startTime)
    val endInstant = DateTimeUtils.localDateTimeToInstant(baseDate, endTime)

    return createEvent(
        title = title,
        startDate = startInstant,
        endDate = endInstant,
    )
  }

  /** Verifies that providing an empty event list results in an empty layout map. */
  @Test
  fun calculateEventLayouts_withEmptyList_returnsEmptyMap() {
    val events = emptyList<Event>()
    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    assertTrue(layouts.isEmpty())
  }

  /**
   * Verifies that non-overlapping events each take the full width and have no horizontal offset.
   */
  @Test
  fun calculateEventLayouts_nonOverlappingEvents_haveFullWidthAndZeroOffset() {
    // Two events that do not overlap in time
    val event1 =
        createEventForTimes(
            title = "Event 1",
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 0,
        )
    val event2 =
        createEventForTimes(
            title = "Event 2",
            startHour = 10,
            startMinute = 0,
            endHour = 11,
            endMinute = 0,
        )
    val events = listOf(event1, event2)

    // Compute layout
    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    // Each event should occupy the full column width with no offset.
    val layout1 = layouts[event1]!!
    val layout2 = layouts[event2]!!

    assertEquals(1.0f, layout1.widthFraction, 0.0001f)
    assertEquals(0.0f, layout1.offsetFraction, 0.0001f)

    assertEquals(1.0f, layout2.widthFraction, 0.0001f)
    assertEquals(0.0f, layout2.offsetFraction, 0.0001f)

    // They should belong to different overlap groups.
    assertTrue(layout1.overlapGroup != layout2.overlapGroup)
  }

  /** Verifies that two overlapping events share the column width and are placed side by side. */
  @Test
  fun calculateEventLayouts_twoOverlappingEvents_shareWidthAndHaveDifferentOffsets() {
    // Two events that overlap in time
    val event1 =
        createEventForTimes(
            title = "Event 1",
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 0,
        )
    val event2 =
        createEventForTimes(
            title = "Event 2",
            startHour = 8,
            startMinute = 30,
            endHour = 9,
            endMinute = 30,
        )
    val events = listOf(event1, event2)

    // Compute layout
    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    val layout1 = layouts[event1]!!
    val layout2 = layouts[event2]!!

    // They should share the width equally (1/2 each).
    assertEquals(0.5f, layout1.widthFraction, 0.0001f)
    assertEquals(0.5f, layout2.widthFraction, 0.0001f)

    // They should have different horizontal offsets: one at 0.0, the other at 0.5.
    assertTrue(layout1.offsetFraction != layout2.offsetFraction)
    val offsets = listOf(layout1.offsetFraction, layout2.offsetFraction).sorted()
    assertEquals(0.0f, offsets[0], 0.0001f)
    assertEquals(0.5f, offsets[1], 0.0001f)

    // They should belong to the same overlap group.
    assertEquals(layout1.overlapGroup, layout2.overlapGroup)
  }

  /**
   * Verifies that events that only touch at their boundaries (end == start) are not considered
   * overlapping.
   */
  @Test
  fun calculateEventLayouts_touchingEvents_areNotConsideredOverlapping() {
    // event1 ends exactly when event2 starts
    val event1 =
        createEventForTimes(
            title = "Event 1",
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 0,
        )
    val event2 =
        createEventForTimes(
            title = "Event 2",
            startHour = 9,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
        )
    val events = listOf(event1, event2)

    // Compute layout
    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    // They should each occupy full width and be in different groups.
    val layout1 = layouts[event1]!!
    val layout2 = layouts[event2]!!

    assertEquals(1.0f, layout1.widthFraction, 0.0001f)
    assertEquals(0.0f, layout1.offsetFraction, 0.0001f)

    assertEquals(1.0f, layout2.widthFraction, 0.0001f)
    assertEquals(0.0f, layout2.offsetFraction, 0.0001f)

    assertTrue(layout1.overlapGroup != layout2.overlapGroup)
  }

  /**
   * Verifies that chained overlaps (A overlaps B, B overlaps C, A does not directly overlap C)
   * result in a single overlap group for all three events.
   */
  @Test
  fun calculateEventLayouts_chainedOverlaps_allInSameGroupAndShareWidth() {
    // A: 08:00 - 09:00
    // B: 08:30 - 09:30
    // C: 09:00 - 10:00
    // A overlaps B, B overlaps C, but A and C only touch at 09:00.
    val eventA =
        createEventForTimes(
            title = "Event A",
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 0,
        )
    val eventB =
        createEventForTimes(
            title = "Event B",
            startHour = 8,
            startMinute = 30,
            endHour = 9,
            endMinute = 30,
        )
    val eventC =
        createEventForTimes(
            title = "Event C",
            startHour = 9,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
        )
    val events = listOf(eventA, eventB, eventC)

    // Compute layout
    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    // All three should be in the same group and share the width 1/3 each.
    val layoutA = layouts[eventA]!!
    val layoutB = layouts[eventB]!!
    val layoutC = layouts[eventC]!!

    assertEquals(layoutA.overlapGroup, layoutB.overlapGroup)
    assertEquals(layoutB.overlapGroup, layoutC.overlapGroup)

    assertEquals(1.0f / 3.0f, layoutA.widthFraction, 0.0001f)
    assertEquals(1.0f / 3.0f, layoutB.widthFraction, 0.0001f)
    assertEquals(1.0f / 3.0f, layoutC.widthFraction, 0.0001f)

    // The three offsets should be distinct and within [0, 1).
    val offsets = listOf(layoutA.offsetFraction, layoutB.offsetFraction, layoutC.offsetFraction)
    assertEquals(3, offsets.toSet().size) // all different
    offsets.forEach { offset ->
      assertTrue(offset >= 0.0f)
      assertTrue(offset < 1.0f)
    }
  }
}
