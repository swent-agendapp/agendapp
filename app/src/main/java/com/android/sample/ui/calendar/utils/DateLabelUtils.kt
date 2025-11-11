package com.android.sample.ui.calendar.utils

import com.android.sample.model.calendar.RecurrenceStatus
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale


/** Human-readable recurrence label in English (for now), with localized weekday short name. */
fun recurrenceLabel(
    status: RecurrenceStatus,
    start: ZonedDateTime,
    locale: Locale
): String =
    when (status) {
        RecurrenceStatus.OneTime -> ""
        RecurrenceStatus.Weekly -> "every week (${weekdayShortLocalized(start.dayOfWeek, locale)})"
        RecurrenceStatus.Monthly -> "every month"
        RecurrenceStatus.Yearly -> "every year"
    }

/** Locale-aware short weekday, e.g. "Mon", "Tue", etc. */
fun weekdayShortLocalized(d: DayOfWeek, locale: Locale): String =
    d.getDisplayName(TextStyle.SHORT, locale)