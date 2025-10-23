package com.android.sample.ui.calendar.data

import java.time.Duration
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TimeSpanTest {

  @Test
  fun `duration lazily computes difference between start and end`() {
    val span = TimeSpan(LocalTime.of(9, 15), LocalTime.of(10, 45))

    assertEquals(Duration.ofMinutes(90), span.duration)
  }

  @Test
  fun `hourlyTimes emits inclusive hours covering span`() {
    val span = TimeSpan(LocalTime.of(8, 30), LocalTime.of(11, 5))

    val ticks = span.hourlyTimes().toList()

    assertEquals(
        listOf(LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0)),
        ticks)
  }

  @Test
  fun `of factory creates span using duration`() {
    val span = TimeSpan.of(LocalTime.of(13, 0), Duration.ofMinutes(90))

    assertEquals(LocalTime.of(13, 0), span.start)
    assertEquals(LocalTime.of(14, 30), span.endExclusive)
  }

  @Test
  fun `start must be strictly before end`() {
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          TimeSpan(LocalTime.NOON, LocalTime.NOON)
        }

    assertEquals("Start time 12:00 must be before end time 12:00!", exception.message)
  }
}
