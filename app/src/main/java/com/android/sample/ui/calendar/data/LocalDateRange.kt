package com.android.sample.ui.calendar.data

import java.time.LocalDate

/** A range of [java.time.LocalDate] values. */
data class LocalDateRange(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
) : ClosedRange<LocalDate>, Iterable<LocalDate> {
  override fun contains(value: LocalDate): Boolean = value >= start && value <= endInclusive

  override fun iterator(): Iterator<LocalDate> =
      object : Iterator<LocalDate> {
        private var nextDate: LocalDate? = start

        override fun hasNext() = nextDate != null

        override fun next(): LocalDate {
          val current = nextDate ?: throw kotlin.NoSuchElementException()
          nextDate = if (current < endInclusive) nextDate!!.plusDays(1) else null
          return current
        }
      }
}
