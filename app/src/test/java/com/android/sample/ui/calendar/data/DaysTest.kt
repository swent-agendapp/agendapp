package com.android.sample.ui.calendar.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DaysTest {

  @Test
  fun `workWeekDays returns monday to friday`() {
    val wednesday = LocalDate.of(2024, 5, 15)

    val week = workWeekDays(wednesday)

    assertEquals(5, week.size)
    assertEquals(LocalDate.of(2024, 5, 13), week.first())
    assertEquals(LocalDate.of(2024, 5, 17), week.last())
  }

  @Test
  fun `daysBetween includes both endpoints`() {
    val start = LocalDate.of(2024, 1, 30)
    val end = LocalDate.of(2024, 2, 2)

    val days = daysBetween(start, end)

    assertEquals(
        listOf(
            LocalDate.of(2024, 1, 30),
            LocalDate.of(2024, 1, 31),
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 2, 2)),
        days)
  }

  @Test
  fun `nextDays returns expected number of days`() {
    val start = LocalDate.of(2023, 12, 28)

    val days = nextDays(start, 3)

    assertEquals(3, days.size)
    assertTrue(
        days.containsAll(
            listOf(
                LocalDate.of(2023, 12, 28),
                LocalDate.of(2023, 12, 29),
                LocalDate.of(2023, 12, 30))))
  }
}
