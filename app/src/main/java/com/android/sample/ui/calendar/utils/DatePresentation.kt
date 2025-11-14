package com.android.sample.ui.calendar.utils

import com.android.sample.model.calendar.Event
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

const val NO_DATA_DEFAULT_VALUE = "No date provided..."

/** Aggregates all date strings and flags needed by the UI. */
data class DatePresentation(
    val isMultiDay: Boolean,
    val dateLine1: String, // single-day line 1
    val dateLine2: String, // single-day line 2
    val startDateShort: String, // multi-day Column 2
    val endDateShort: String, // multi-day Column 2
    val startTimeStr: String,
    val endTimeStr: String,
    val startZdt: ZonedDateTime,
    val endZdt: ZonedDateTime
)

/** Builds a stable, reusable description of how dates should be rendered on the card. */
fun buildDatePresentation(event: Event, zoneId: ZoneId, locale: Locale): DatePresentation {
  val startZdt = event.startDate.atZone(zoneId)
  val endZdt = event.endDate.atZone(zoneId)
  val isMulti = startZdt.toLocalDate() != endZdt.toLocalDate()

  val dayFull = DateTimeFormatter.ofPattern("EEEE d MMM yyyy", locale)
  val dayShort = DateTimeFormatter.ofPattern("EEE d MMM yyyy", locale)
  val hm = DateTimeFormatter.ofPattern("HH:mm", locale)

  val dateLine1: String
  val dateLine2: String
  if (!isMulti) {
    // "Monday 1 Dec 2025"
    dateLine1 = startZdt.format(dayFull)
    // "10:00 — 12:00"
    dateLine2 = "${startZdt.format(hm)} — ${endZdt.format(hm)}"
  } else {
    // Use a consistent structure with the single-day version for easier reuse and readability
    dateLine1 = "From ${startZdt.format(dayShort)} at ${startZdt.format(hm)}"
    dateLine2 = "${startZdt.format(hm)} — ${endZdt.format(hm)}"
  }

  return DatePresentation(
      isMultiDay = isMulti,
      dateLine1 = dateLine1,
      dateLine2 = dateLine2,
      startDateShort = startZdt.format(dayShort),
      endDateShort = endZdt.format(dayShort),
      startTimeStr = startZdt.format(hm),
      endTimeStr = endZdt.format(hm),
      startZdt = startZdt,
      endZdt = endZdt)
}
