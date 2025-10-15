package com.android.sample.ui.calendar.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val localTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

/**
 * Formats this [LocalTime] using the device's localized short time style
 * (e.g., "14:00" or "2:00 PM" depending on locale and 24h preference).
 *
 * @return A locale-aware short time string.
 */
internal fun LocalTime.toLocalString(): String {
  return localTimeFormat.format(this)
}
