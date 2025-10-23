package com.android.sample.ui.calendar.utils

import androidx.compose.ui.unit.Density
import com.android.sample.ui.calendar.data.TimeSpan
import com.android.sample.ui.calendar.mockData.MockEvent
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class EventPositionUtilTest {

  private val density = Density(1f)
  private val baseEvent =
      MockEvent(
          date = LocalDate.of(2024, 5, 1),
          title = "Planning",
          timeSpan = TimeSpan.of(LocalTime.of(9, 0), Duration.ofMinutes(60)),
          assigneeName = "Team",
          backgroundColor = 0xFF0000)

  @Test
  fun `calculateVerticalOffsets uses event start relative to window`() {
    val event = baseEvent.copy(timeSpan = TimeSpan.of(LocalTime.of(9, 30), Duration.ofMinutes(45)))

    val (top, height) =
        EventPositionUtil.calculateVerticalOffsets(event, LocalTime.of(8, 0), density)

    assertEquals(90f, top.value, 0.001f)
    assertEquals(45f, height.value, 0.001f)
  }

  @Test
  fun `calculateVerticalOffsets clamps events starting before window`() {
    val earlyEvent =
        baseEvent.copy(timeSpan = TimeSpan.of(LocalTime.of(6, 0), Duration.ofMinutes(30)))

    val (top, _) =
        EventPositionUtil.calculateVerticalOffsets(earlyEvent, LocalTime.of(8, 0), density)

    assertEquals(0f, top.value, 0.001f)
  }
}
