package com.android.sample.ui.calendar.utils

import com.android.sample.model.calendar.Event

/**
 * Utility object for calculating horizontal overlap layouts for events. It assumes all events
 * belong to the same day column (as enforced by the caller).
 */
object EventOverlapCalculator {

  /**
   * Layout information for a single event within a column.
   *
   * @param widthFraction Fraction of the column width this event should occupy (0.0 to 1.0).
   * @param offsetFraction Horizontal offset fraction from the left edge of the column (0.0 to 1.0).
   * @param overlapGroup Group index this event belongs to (useful for debugging or future
   *   extensions).
   */
  data class EventLayout(
      val widthFraction: Float,
      val offsetFraction: Float,
      val overlapGroup: Int,
  )

  /**
   * Calculates layout information for a set of events that will be rendered in the same day column.
   *
   * Overlapping events are placed side by side, each taking 1 / N of the column width, where N is
   * the size of its overlap group.
   *
   * @param events List of events in a single day column.
   * @return Map from Event to its layout information.
   */
  fun calculateEventLayouts(events: List<Event>): Map<Event, EventLayout> {
    val layoutMap = mutableMapOf<Event, EventLayout>()
    if (events.isEmpty()) return layoutMap

    // Sort events by start time for consistent processing
    val sortedEvents = events.sortedBy { it.startDate }
    val eventCount = sortedEvents.size
    val visited = BooleanArray(eventCount)
    var groupIndex = 0

    // Build overlap graph (adjacency list)
    val adjacency = Array(eventCount) { mutableListOf<Int>() }
    for (i in 0 until eventCount) {
      for (j in i + 1 until eventCount) {
        if (eventsOverlap(sortedEvents[i], sortedEvents[j])) {
          adjacency[i].add(j)
          adjacency[j].add(i)
        }
      }
    }

    // Find connected components (overlap groups) and assign layout
    for (i in 0 until eventCount) {
      if (visited[i]) continue

      val group = mutableListOf<Int>()
      val queue = ArrayDeque<Int>()
      visited[i] = true
      queue.add(i)

      while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        group.add(current)
        for (neighbor in adjacency[current]) {
          if (!visited[neighbor]) {
            visited[neighbor] = true
            queue.add(neighbor)
          }
        }
      }

      if (group.isEmpty()) continue

      val widthPerEvent = 1.0f / group.size

      // Order events inside the group by start time for a stable horizontal ordering
      val orderedGroup = group.sortedBy { idx -> sortedEvents[idx].startDate }

      orderedGroup.forEachIndexed { columnIndex, eventIdx ->
        val event = sortedEvents[eventIdx]
        layoutMap[event] =
            EventLayout(
                widthFraction = widthPerEvent,
                offsetFraction = columnIndex * widthPerEvent,
                overlapGroup = groupIndex,
            )
      }

      groupIndex++
    }

    return layoutMap
  }

  /**
   * Determines if two events overlap in time (on the same day column). Intervals are treated
   * as [start, end).
   */
  private fun eventsOverlap(
      event1: Event,
      event2: Event,
  ): Boolean {
    val start1 = event1.startDate
    val end1 = event1.endDate
    val start2 = event2.startDate
    val end2 = event2.endDate

    // Non-empty intersection of [start1, end1[ and [start2, end2[
    return start1 < end2 && end1 > start2
  }
}
