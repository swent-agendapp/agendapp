package com.android.sample.ui.calendar.utils

import com.android.sample.model.calendar.RecurrenceStatus
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Builds a short human-readable recurrence label in English (for now), with a locale-aware weekday
 * for weekly events.
 *
 * Note: Only the weekday part is localized via [weekdayShortLocalized], the leading phrase ("every
 * week/month/year") is currently English-only and can be externalized later.
 *
 * @param status Recurrence status.
 * @param start Start date/time (used to derive the weekday for weekly recurrences).
 * @param locale Locale used for weekday localization.
 * @return A short single-line label, empty string for one-off events.
 */
fun recurrenceLabel(status: RecurrenceStatus, start: ZonedDateTime, locale: Locale): String =
    when (status) {
      RecurrenceStatus.OneTime -> ""
      RecurrenceStatus.Weekly -> "every week (${weekdayShortLocalized(start.dayOfWeek, locale)})"
      RecurrenceStatus.Monthly -> "every month"
      RecurrenceStatus.Yearly -> "every year"
    }

/** @return Locale-aware short weekday name (e.g., "Mon", "Lun", "Mo."). */
fun weekdayShortLocalized(d: DayOfWeek, locale: Locale): String =
    d.getDisplayName(TextStyle.SHORT, locale)
