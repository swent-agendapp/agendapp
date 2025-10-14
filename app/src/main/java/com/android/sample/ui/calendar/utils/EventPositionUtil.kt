package com.android.sample.ui.calendar.utils

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.mockData.MockEvent
import java.time.LocalTime

object EventPositionUtil {
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
