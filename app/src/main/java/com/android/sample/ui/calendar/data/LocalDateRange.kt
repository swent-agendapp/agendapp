package com.android.sample.ui.calendar.data

import java.time.LocalDate

/**
 * Inclusive range of [LocalDate] values with iterable semantics.
 *
 * @property start First date in the range.
 * @property endInclusive Last date in the range (included).
 * @throws IllegalArgumentException if [endInclusive] is before [start].
 */
data class LocalDateRange(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
) : ClosedRange<LocalDate>, Iterable<LocalDate> {
  init {
    require(!endInclusive.isBefore(start)) {
      "start ($start) must not be after endInclusive ($endInclusive)"
    }
  }

  override fun contains(value: LocalDate): Boolean = value >= start && value <= endInclusive

    /**
     * Iterates day by day from [start] to [endInclusive].
     *
     * @return An iterator yielding each date in ascending order.
     */
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
