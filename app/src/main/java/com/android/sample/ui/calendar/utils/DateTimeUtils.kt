package com.android.sample.ui.calendar.utils

import java.time.*
import java.time.format.DateTimeFormatter

object DateTimeUtils {

  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

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
}
