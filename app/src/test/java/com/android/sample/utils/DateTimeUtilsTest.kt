package com.android.sample.utils

import com.android.sample.ui.calendar.utils.DateTimeUtils.formatInstantToDate
import com.android.sample.ui.calendar.utils.DateTimeUtils.formatInstantToTime
import com.android.sample.ui.calendar.utils.DateTimeUtils.formatLocalDateTimeToDate
import com.android.sample.ui.calendar.utils.DateTimeUtils.formatLocalDateTimeToTime
import com.android.sample.ui.calendar.utils.DateTimeUtils.localDateTimeToInstant
import com.android.sample.ui.calendar.utils.DateTimeUtils.parseDateTimeToInstant
import java.time.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DateTimeUtils utility functions. These tests verify the correct formatting and
 * parsing of date and time values.
 */
class DateTimeUtilsTest {

  // ZoneId used for all tests, set to system default
  private lateinit var zoneId: ZoneId
  private lateinit var instant: Instant
  private lateinit var localDate: LocalDate
  private lateinit var localTime: LocalTime

  @Before
  fun setUp() {
    zoneId = ZoneId.systemDefault()
    localDate = LocalDate.of(2025, 3, 10)
    localTime = LocalTime.of(14, 30)
    instant = LocalDateTime.of(localDate, localTime).atZone(zoneId).toInstant()
  }

  @Test
  fun formatInstantToDate_shouldReturnCorrectDateString() {
    val result = formatInstantToDate(instant)
    assertEquals("10/03/2025", result)
  }

  @Test
  fun formatInstantToTime_shouldReturnCorrectTimeString() {
    val result = formatInstantToTime(instant)
    assertEquals("14:30", result)
  }

  @Test
  fun parseDateTimeToInstant_shouldReturnSameInstantAsOriginal() {
    val dateStr = "10/03/2025"
    val timeStr = "14:30"
    val parsedInstant = parseDateTimeToInstant(dateStr, timeStr)

    assertEquals(instant.epochSecond, parsedInstant.epochSecond)
  }

  @Test
  fun localDateTimeToInstant_shouldMatchManualConversion() {
    val expected = LocalDateTime.of(localDate, localTime).atZone(zoneId).toInstant()
    val result = localDateTimeToInstant(localDate, localTime)

    assertEquals(expected, result)
  }

  @Test
  fun formatLocalDateTimeToDate_shouldMatchFormatInstantToDate() {
    val expected = formatInstantToDate(instant)
    val result = formatLocalDateTimeToDate(localDate, localTime)

    assertEquals(expected, result)
  }

  @Test
  fun formatLocalDateTimeToTime_shouldMatchFormatInstantToTime() {
    val expected = formatInstantToTime(instant)
    val result = formatLocalDateTimeToTime(localDate, localTime)

    assertEquals(expected, result)
  }

  @Test
  fun parseDateTimeToInstant_shouldThrowWhenInvalidFormat() {
    assertThrows(DateTimeException::class.java) { parseDateTimeToInstant("2025-03-10", "14:30") }
  }
}
