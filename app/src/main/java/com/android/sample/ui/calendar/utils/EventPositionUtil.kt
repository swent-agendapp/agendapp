package com.android.sample.ui.calendar.utils

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.mockData.MockEvent
import java.time.LocalTime

/** Utility functions to compute layout positions and sizes for event blocks. */
object EventPositionUtil {
    /**
     * Computes the top offset and height (both in [Dp]) for a given [event] relative to a visible
     * [startTime].
     *
     * @param event The event to layout.
     * @param startTime The inclusive start of the visible time window.
     * @param density Current Compose [Density] used to convert minutes to dp.
     * @return A pair of (topOffset, height) in dp.
     */
  fun calculateVerticalOffsets(
      event: MockEvent,
      startTime: LocalTime,
      density: Density,
  ): Pair<Dp, Dp> {
    // Minutes since the start of the visible column
    val startMinutes =
        (event.timeSpan.start.hour - startTime.hour) * 60 +
            (event.timeSpan.start.minute - startTime.minute)
    // Duration of the event in minutes
    val durationMinutes = event.timeSpan.duration.toMinutes().toInt()

    // Clamp negative offsets to zero so events before the visible start are not shown above the
    // grid
    val clampedStartMinutes = startMinutes.coerceAtLeast(0)

    // Convert to dp
    val topOffset = with(density) { (clampedStartMinutes /* Later : * scalingFactor */).dp }
    val eventHeight = with(density) { (durationMinutes /* Later : * scalingFactor */).dp }

    return Pair(topOffset, eventHeight)
  }
}
