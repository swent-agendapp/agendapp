package com.android.sample.ui.calendar.utils

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.sample.model.calendar.Event
import java.time.Duration
import java.time.LocalTime
import java.time.LocalDate

/** Utility functions to compute layout positions and sizes for event blocks. */
object EventPositionUtil {
  /**
   * Computes the top offset and height (both in [Dp]) for the *visible portion* of a given [event]
   * in the day column identified by [currentDate], within the visible time window
   * [startTime, endTime).
   *
   * This method correctly handles events that:
   *  - start before the visible window and/or end after it (are clipped),
   *  - span across midnight into the next/previous day,
   *  - have zero or negative intersections after filtering (safeguarded).
   *
   * The computation is performed entirely in Instants to avoid timezone and day-boundary pitfalls.
   *
   * @param event The event to layout.
   * @param currentDate The date of the column in which the event (or a clipped portion) appears.
   * @param startTime The inclusive start of the visible time window for this column.
   * @param endTime The exclusive end of the visible time window for this column.
   * @param density Current Compose [Density] used to convert minutes to dp.
   * @return A pair of (topOffset, height) in dp for the *clipped* segment.
   */
  fun calculateVerticalOffsets(
      event: Event,
      currentDate: LocalDate,
      startTime: LocalTime,
      endTime: LocalTime,
      density: Density,
  ): Pair<Dp, Dp> {
    // Build the visible window in absolute time for the given day column
    val visibleStartInstant = DateTimeUtils.localDateTimeToInstant(currentDate, startTime)
    val visibleEndInstantExclusive = DateTimeUtils.localDateTimeToInstant(currentDate, endTime)

    // Intersect [event.startDate, event.endDate) with [visibleStartInstant, visibleEndInstantExclusive)
    val segmentStart = maxOf(event.startDate, visibleStartInstant)
    val segmentEnd = minOf(event.endDate, visibleEndInstantExclusive)

    // If no overlap remains, return zero-sized segment at the top of the window to avoid crashes
    if (segmentEnd <= segmentStart) {
      return 0.dp to 0.dp
    }

    // Compute the vertical offset from the top of the visible window in minutes
    val startMinutes = Duration.between(visibleStartInstant, segmentStart).toMinutes()
        .coerceAtLeast(0) // for safety
        .toInt()

    // Compute the height in minutes for the clipped segment only
    val durationMinutes = Duration.between(segmentStart, segmentEnd).toMinutes()
        .coerceAtLeast(0)
        .toInt()

    // Convert minutes to dp
    val topOffset = with(density) { (startMinutes /* Later : * scalingFactor */).dp }
    val eventHeight = with(density) { (durationMinutes /* Later : * scalingFactor */).dp }

    return topOffset to eventHeight
  }
}
