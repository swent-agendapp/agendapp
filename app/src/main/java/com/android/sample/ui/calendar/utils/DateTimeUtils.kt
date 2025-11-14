package com.android.sample.ui.calendar.utils

import java.time.*
import java.time.format.DateTimeFormatter



object DateTimeUtils {
  const val DATE_FORMAT_PATTERN = "dd/MM/yyyy"
  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  fun formatInstantToDate(instant: Instant): String {
    return instant.atZone(zoneId).toLocalDate().format(dateFormatter)
  }

  fun formatInstantToTime(instant: Instant): String {
    return instant.atZone(zoneId).toLocalTime().format(timeFormatter)
  }

  fun parseDateTimeToInstant(date: String, time: String): Instant {
    val localDate = LocalDate.parse(date, dateFormatter)
    val localTime = LocalTime.parse(time, timeFormatter)
    return LocalDateTime.of(localDate, localTime).atZone(zoneId).toInstant()
  }

  fun localDateTimeToInstant(localDate: LocalDate, localTime: LocalTime): Instant {
    val localDateTime = LocalDateTime.of(localDate, localTime)
    return localDateTime.atZone(zoneId).toInstant()
  }

  fun formatLocalDateTimeToDate(localDate: LocalDate, localTime: LocalTime): String {
    return formatInstantToDate(localDateTimeToInstant(localDate, localTime))
  }

  fun formatLocalDateTimeToTime(localDate: LocalDate, localTime: LocalTime): String {
    return formatInstantToTime(localDateTimeToInstant(localDate, localTime))
  }

  fun instantWithTime(
      instant: Instant,
      hour: Int,
      minute: Int,
      zone: ZoneId = ZoneId.systemDefault()
  ): Instant {
    return instant.atZone(zone).withHour(hour).withMinute(minute).toInstant()
  }

  fun instantWithDate(
      instant: Instant,
      date: LocalDate,
      zone: ZoneId = ZoneId.systemDefault()
  ): Instant {
    return instant
        .atZone(zone)
        .withYear(date.year)
        .withMonth(date.monthValue)
        .withDayOfMonth(date.dayOfMonth)
        .toInstant()
  }

  fun getInstantHour(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): Int {
    return instant.atZone(zone).hour
  }

  fun getInstantMinute(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): Int {
    return instant.atZone(zone).minute
  }

  fun nowInstantPlusHours(hours: Long): Instant {
    return Instant.now().plusSeconds(hours * 3600)
  }

  fun dayStartInstant(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Instant {
    return date.atStartOfDay(zone).toInstant()
  }

  /**
   * End of the given day as an exclusive bound (for example the start of the next day). Prefer
   * using this exclusive bound for interval-overlap checks.
   */
  fun dayEndInstantExclusive(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Instant {
    return date.plusDays(1).atStartOfDay(zone).toInstant()
  }
}
