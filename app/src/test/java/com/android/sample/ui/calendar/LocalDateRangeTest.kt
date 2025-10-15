package com.android.sample.ui.calendar

import com.android.sample.ui.calendar.data.LocalDateRange
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class LocalRepositoryTest {
  private val base = LocalDate.of(2025, 1, 1)

  @Test
  fun sevenDayRange_firstHasNext() {
    val it = LocalDateRange(base, base).iterator()
    assertEquals(true, it.hasNext())
    assertEquals(base, it.next())
    assertEquals(false, it.hasNext())
  }

  @Test
  fun sevenDayRange_thirdHasNext() {
    val dateRange = LocalDateRange(base, base.plusDays(6))
    val iterator = dateRange.iterator()

    for (i in 1..3) {
      iterator.next()
    }
    assertEquals(true, iterator.hasNext())
  }

  @Test
  fun sevenDayRange_afterConsumingAll_hasNoNext() {
    val dateRange = LocalDateRange(base, base.plusDays(6))
    val iterator = dateRange.iterator()

    for (i in 1..7) {
      iterator.next()
    }
    assertEquals(false, iterator.hasNext())
  }

  @Test
  fun sevenDayRange_countIs7() {
    val dateRange = LocalDateRange(base, base.plusDays(6))
    var i = 0
    val iterator = dateRange.iterator()
    while (iterator.hasNext()) {
      i++
      iterator.next()
    }
    assertEquals(7, i)
  }

  @Test
  fun sevenDayRange_returnsConsecutiveDays_inOrder() {
    val dateRange = LocalDateRange(base, base.plusDays(6))
    val iterator = dateRange.iterator()
    var i = 0L

    while (iterator.hasNext()) {
      assertEquals(base.plusDays(i), iterator.next())
      i++
    }
  }

  @Test
  fun constructor_throwsIfEndBeforeStart() {
    assertThrows(IllegalArgumentException::class.java) {
      LocalDateRange(LocalDate.parse("2025-10-10"), LocalDate.parse("2025-10-05"))
    }
  }

  @Test
  fun oneDayRange_doesContainCurrentElem() {
    val dateRange = LocalDateRange(base, base)
    assertEquals(true, dateRange.contains(base))
  }

  @Test
  fun oneDayRange_doesNotContainNextElem() {
    val dateRange = LocalDateRange(base, base)
    assertEquals(false, dateRange.contains(base.plusDays(1)))
  }

  @Test
  fun sevenDayRange_containAllWeek() {
    val dateRange = LocalDateRange(base, base.plusDays(6))
    val iterator = dateRange.iterator()

    while (iterator.hasNext()) {
      assertEquals(true, dateRange.contains(iterator.next()))
    }
  }

  @Test
  fun contains_inclusiveBounds_and_outside() {
    val dateRange = LocalDateRange(base, base.plusDays(6))

    assertEquals(false, dateRange.contains(base.plusDays(7)))
    assertEquals(false, dateRange.contains(base.plusDays(-1)))
  }
}
