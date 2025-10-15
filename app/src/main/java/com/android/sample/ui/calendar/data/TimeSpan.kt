package com.android.sample.ui.calendar.data

import java.time.Duration
import java.time.LocalTime

/**
 * Half-open time interval [start, endExclusive) used for grid and event layout.
 *
 * @property start Inclusive start time.
 * @property endExclusive Exclusive end time; must be strictly after [start].
 * @throws IllegalArgumentException if [start] is not before [endExclusive].
 */
data class TimeSpan(
    val start: LocalTime = LocalTime.of(8, 0),
    val endExclusive: LocalTime = LocalTime.of(9, 0),
) {
  init {
    require(start.isBefore(endExclusive)) {
      "Start time $start must be before end time $endExclusive!"
    }
  }

  val duration: Duration by lazy { Duration.between(start, endExclusive) }

  /**
   * Produces hour-aligned labels that cover this span, including the hour containing [start]
   * and the hour containing [endExclusive].
   * Example: TimeSpan from 08:30 to 12:15 would return [08:00, 09:00, 10:00, 11:00, 12:00]
   *
   * @return A sequence of hour ticks (00 minutes) in ascending order.
   */
  fun hourlyTimes(): Sequence<LocalTime> = sequence {
    var currentHour = start.hour
    val endHour = endExclusive.hour

    // Always yield the starting hour
    yield(LocalTime.of(currentHour, 0))

    // Generate subsequent hours until we reach the end
    // Use <= to include the hour containing the end time
    while (currentHour < endHour) {
      currentHour++
      if (currentHour <= endHour) {
        yield(LocalTime.of(currentHour, 0))
      }
    }
  }

  /**
   * Factory to create a [TimeSpan] from a [start] and a [duration].
   *
   * @param start Inclusive start time.
   * @param duration Duration length to add to [start].
   * @return A new [TimeSpan] with [endExclusive] = start + duration.
   */
  companion object {
    fun of(
        start: LocalTime,
        duration: Duration,
    ): TimeSpan {
      return TimeSpan(start, start.plus(duration))
    }
  }
}
