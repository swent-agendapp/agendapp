package com.android.sample.ui.calendar

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.createEventForTimes
import com.android.sample.ui.calendar.utils.EventOverlapCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val DELTA = 0.0001f

/**
 * Unit tests for [EventOverlapCalculator].
 *
 * These tests focus on:
 * - Empty input.
 * - Non-overlapping events and events that only touch at their boundaries.
 * - Simple overlapping events.
 * - Chained overlaps where at most two events overlap at the same time.
 * - Cases with three events fully overlapping.
 * - Cases with multiple independent clusters.
 * - The fact that the result does not depend on the input order.
 */
class EventOverlapCalculatorTest {
  /** Verifies that providing an empty event list results in an empty layout map. */
  @Test
  fun calculateEventLayouts_withEmptyList_returnsEmptyMap() {
    val events = emptyList<Event>()

    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    assertTrue(layouts.isEmpty())
  }

  /**
   * Verifies that non-overlapping events each take the full width and have no horizontal offset.
   *
   * They are also placed in different overlap groups because they belong to different clusters.
   */
  @Test
  fun calculateEventLayouts_nonOverlappingEvents_haveFullWidthAndZeroOffset() {
    // Two events that do not overlap in time.
    val event1 =
        createEventForTimes(
            title = "Event 1",
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 0,
        )[0]
    val event2 =
        createEventForTimes(
            title = "Event 2",
            startHour = 10,
            startMinute = 0,
            endHour = 11,
            endMinute = 0,
        )[0]
    val events = listOf(event1, event2)

    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    val layout1 = layouts[event1]!!
    val layout2 = layouts[event2]!!

    // Each event should occupy the full column width with no offset.
    assertEquals(1.0f, layout1.widthFraction, DELTA)
    assertEquals(0.0f, layout1.offsetFraction, DELTA)

    assertEquals(1.0f, layout2.widthFraction, DELTA)
    assertEquals(0.0f, layout2.offsetFraction, DELTA)

    // They should belong to different overlap groups (different clusters).
    assertTrue(layout1.overlapGroup != layout2.overlapGroup)
  }

  /**
   * Verifies that events that only touch at their boundaries (end == start) are not considered
   * overlapping.
   *
   * They should behave like non-overlapping events.
   */
  @Test
  fun calculateEventLayouts_touchingEvents_areNotConsideredOverlapping() {
    // event1 ends exactly when event2 starts.
    val event1 =
        createEventForTimes(
            title = "Event 1",
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 0,
        )[0]
    val event2 =
        createEventForTimes(
            title = "Event 2",
            startHour = 9,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
        )[0]
    val events = listOf(event1, event2)

    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    val layout1 = layouts[event1]!!
    val layout2 = layouts[event2]!!

    // They should each occupy full width with no horizontal offset.
    assertEquals(1.0f, layout1.widthFraction, DELTA)
    assertEquals(0.0f, layout1.offsetFraction, DELTA)

    assertEquals(1.0f, layout2.widthFraction, DELTA)
    assertEquals(0.0f, layout2.offsetFraction, DELTA)

    // They should be in different overlap groups because they are in different clusters.
    assertTrue(layout1.overlapGroup != layout2.overlapGroup)
  }

  /** Verifies that two overlapping events share the width and are placed side by side. */
  @Test
  fun calculateEventLayouts_twoOverlappingEvents_shareWidthAndHaveDifferentOffsets() {
    // Two events that overlap in time.
    val event1 =
        createEventForTimes(
            title = "Event 1",
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 0,
        )[0]
    val event2 =
        createEventForTimes(
            title = "Event 2",
            startHour = 8,
            startMinute = 30,
            endHour = 9,
            endMinute = 30,
        )[0]
    val events = listOf(event1, event2)

    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    val layout1 = layouts[event1]!!
    val layout2 = layouts[event2]!!

    // They should share the width equally (1/2 each).
    assertEquals(0.5f, layout1.widthFraction, DELTA)
    assertEquals(0.5f, layout2.widthFraction, DELTA)

    // They should have different horizontal offsets: one at 0.0, the other at 0.5.
    assertTrue(layout1.offsetFraction != layout2.offsetFraction)
    val offsets = listOf(layout1.offsetFraction, layout2.offsetFraction).sorted()
    assertEquals(0.0f, offsets[0], DELTA)
    assertEquals(0.5f, offsets[1], DELTA)

    // They should belong to the same overlap group (same cluster).
    assertEquals(layout1.overlapGroup, layout2.overlapGroup)
  }

  /**
   * Verifies that chained overlaps (A overlaps B, B overlaps C, A and C only touch) still behave
   * like a case where at most two events overlap at the same time.
   *
   * All three events share the same cluster, but each event uses half of the width because the
   * maximum number of simultaneous events is two.
   */
  @Test
  fun calculateEventLayouts_chainedOverlaps_maxTwoSimultaneous_useHalfWidthEach() {
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
        )[0]
    val eventB =
        createEventForTimes(
            title = "Event B",
            startHour = 8,
            startMinute = 30,
            endHour = 9,
            endMinute = 30,
        )[0]
    val eventC =
        createEventForTimes(
            title = "Event C",
            startHour = 9,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
        )[0]
    val events = listOf(eventA, eventB, eventC)

    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    val layoutA = layouts[eventA]!!
    val layoutB = layouts[eventB]!!
    val layoutC = layouts[eventC]!!

    // All three belong to the same overlap group (same cluster).
    assertEquals(layoutA.overlapGroup, layoutB.overlapGroup)
    assertEquals(layoutB.overlapGroup, layoutC.overlapGroup)

    // At most two events are active at the same time, so each event uses half of the width.
    assertEquals(0.5f, layoutA.widthFraction, DELTA)
    assertEquals(0.5f, layoutB.widthFraction, DELTA)
    assertEquals(0.5f, layoutC.widthFraction, DELTA)

    // There should be exactly two distinct offsets: 0.0 and 0.5.
    val offsets = listOf(layoutA.offsetFraction, layoutB.offsetFraction, layoutC.offsetFraction)
    val distinctOffsets = offsets.toSet().sorted()
    assertEquals(2, distinctOffsets.size)
    assertEquals(0.0f, distinctOffsets[0], DELTA)
    assertEquals(0.5f, distinctOffsets[1], DELTA)
  }

  /**
   * Verifies that three events that all overlap at the same time share the width in thirds and use
   * three different offsets.
   */
  @Test
  fun calculateEventLayouts_threeFullyOverlappingEvents_useThirdOfWidthEach() {
    // All three overlap between 08:45 and 09:15.
    val event1 =
        createEventForTimes(
            title = "Event 1",
            startHour = 8,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
        )[0]
    val event2 =
        createEventForTimes(
            title = "Event 2",
            startHour = 8,
            startMinute = 30,
            endHour = 9,
            endMinute = 30,
        )[0]
    val event3 =
        createEventForTimes(
            title = "Event 3",
            startHour = 8,
            startMinute = 45,
            endHour = 9,
            endMinute = 15,
        )[0]
    val events = listOf(event1, event2, event3)

    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    val layout1 = layouts[event1]!!
    val layout2 = layouts[event2]!!
    val layout3 = layouts[event3]!!

    // All in the same overlap group (same cluster).
    assertEquals(layout1.overlapGroup, layout2.overlapGroup)
    assertEquals(layout2.overlapGroup, layout3.overlapGroup)

    // Three events overlap, so each one uses one third of the width.
    val third = 1.0f / 3.0f
    assertEquals(third, layout1.widthFraction, DELTA)
    assertEquals(third, layout2.widthFraction, DELTA)
    assertEquals(third, layout3.widthFraction, DELTA)

    // Offsets should be three distinct values within [0, 1).
    val offsets =
        listOf(layout1.offsetFraction, layout2.offsetFraction, layout3.offsetFraction).sorted()
    assertEquals(3, offsets.toSet().size)
    assertEquals(0.0f, offsets[0], DELTA)
    assertEquals(third, offsets[1], DELTA)
    assertEquals(2 * third, offsets[2], DELTA)
  }

  /**
   * Verifies that two separate clusters are assigned different overlap groups and that their
   * internal layouts are computed independently.
   */
  @Test
  fun calculateEventLayouts_separateClusters_haveIndependentGroups() {
    // Cluster 1: two overlapping events in the morning.
    val morningEvent1 =
        createEventForTimes(
            title = "Morning 1",
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 0,
        )[0]
    val morningEvent2 =
        createEventForTimes(
            title = "Morning 2",
            startHour = 8,
            startMinute = 30,
            endHour = 9,
            endMinute = 30,
        )[0]

    // Cluster 2: two overlapping events in the afternoon.
    val afternoonEvent1 =
        createEventForTimes(
            title = "Afternoon 1",
            startHour = 12,
            startMinute = 0,
            endHour = 13,
            endMinute = 0,
        )[0]
    val afternoonEvent2 =
        createEventForTimes(
            title = "Afternoon 2",
            startHour = 12,
            startMinute = 30,
            endHour = 13,
            endMinute = 30,
        )[0]

    val events = listOf(morningEvent1, morningEvent2, afternoonEvent1, afternoonEvent2)

    val layouts = EventOverlapCalculator.calculateEventLayouts(events)

    val morningLayout1 = layouts[morningEvent1]!!
    val morningLayout2 = layouts[morningEvent2]!!
    val afternoonLayout1 = layouts[afternoonEvent1]!!
    val afternoonLayout2 = layouts[afternoonEvent2]!!

    // Events in the morning share a group.
    assertEquals(morningLayout1.overlapGroup, morningLayout2.overlapGroup)

    // Events in the afternoon share another group.
    assertEquals(afternoonLayout1.overlapGroup, afternoonLayout2.overlapGroup)

    // Morning and afternoon groups should be different.
    assertTrue(morningLayout1.overlapGroup != afternoonLayout1.overlapGroup)

    // Inside each cluster, the two events should share the width equally.
    assertEquals(0.5f, morningLayout1.widthFraction, DELTA)
    assertEquals(0.5f, morningLayout2.widthFraction, DELTA)

    assertEquals(0.5f, afternoonLayout1.widthFraction, DELTA)
    assertEquals(0.5f, afternoonLayout2.widthFraction, DELTA)
  }

  /**
   * Verifies that the result is independent of the input order.
   *
   * The same set of events with a different order should produce the same layout for each event.
   */
  @Test
  fun calculateEventLayouts_isIndependentOfInputOrder() {
    val event1 =
        createEventForTimes(
            title = "Event 1",
            startHour = 8,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
        )[0]
    val event2 =
        createEventForTimes(
            title = "Event 2",
            startHour = 8,
            startMinute = 30,
            endHour = 9,
            endMinute = 30,
        )[0]
    val event3 =
        createEventForTimes(
            title = "Event 3",
            startHour = 9,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
        )[0]

    val eventsInOrder = listOf(event1, event2, event3)
    val eventsReversed = listOf(event3, event2, event1)

    val layouts1 = EventOverlapCalculator.calculateEventLayouts(eventsInOrder)
    val layouts2 = EventOverlapCalculator.calculateEventLayouts(eventsReversed)

    // For each event, the width and offset should be the same in both maps.
    listOf(event1, event2, event3).forEach { event ->
      val layoutA = layouts1[event]!!
      val layoutB = layouts2[event]!!

      assertEquals(layoutA.widthFraction, layoutB.widthFraction, DELTA)
      assertEquals(layoutA.offsetFraction, layoutB.offsetFraction, DELTA)
      assertEquals(layoutA.overlapGroup, layoutB.overlapGroup)
    }
  }
}
