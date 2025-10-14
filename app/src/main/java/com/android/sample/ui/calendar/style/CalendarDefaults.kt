package com.android.sample.ui.calendar.style

import com.android.sample.ui.calendar.data.LocalDateRange
import java.time.Duration
import java.time.LocalTime

data object CalendarDefaults {
  val DefaultStartTime: LocalTime = LocalTime.of(8, 0)

  val DefaultEndTime: LocalTime = LocalTime.of(23, 0)

  // initialize the week from monday to friday
  val DefaultDateRange: LocalDateRange = run {
    val today = java.time.LocalDate.now()
    val initialStartOfWeek = today.with(java.time.DayOfWeek.MONDAY)
    val initialEndOfWeek = today.with(java.time.DayOfWeek.FRIDAY)
    LocalDateRange(initialStartOfWeek, initialEndOfWeek)
  }

  const val DefaultDaysInWeek: Int = 5

  val DefaultTotalHour = Duration.between(DefaultStartTime, DefaultEndTime).toHours().toInt()

  val strokeWidthDefault = 2f
}
