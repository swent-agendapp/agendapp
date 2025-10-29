package com.android.sample.ui.calendar.style

import com.android.sample.ui.calendar.data.LocalDateRange
import java.time.Duration
import java.time.LocalTime

/** Central defaults for the calendar UI: start/end times, default work-week range, and sizing. */
data object CalendarDefaults {
  /** Default inclusive start time of the visible grid. */
  val DefaultStartTime: LocalTime = LocalTime.of(8, 0)

  /** Default exclusive end time of the visible grid. */
  val DefaultEndTime: LocalTime = LocalTime.of(23, 0)

  /** Default initial week range: Monday to Friday of the current week at app launch. */
  val DefaultDateRange: LocalDateRange = run {
    val today = java.time.LocalDate.now()
    val initialStartOfWeek = today.with(java.time.DayOfWeek.MONDAY)
    // later : default view will be 5-days view, except if today is on the weekend
    // for now :
    val initialEndOfWeek = today.with(java.time.DayOfWeek.SUNDAY)
    LocalDateRange(initialStartOfWeek, initialEndOfWeek)
  }

  /** Default number of days in the work-week grid. */
  const val DefaultDaysInWeek: Int = 7 // for now, later the default will be 5, except when today is on the weekend

  /** Derived total hours between start and end times. */
  val DefaultTotalHour = Duration.between(DefaultStartTime, DefaultEndTime).toHours().toInt()

  /** Default stroke width (in pixels) for grid lines. */
  val strokeWidthDefault = 2f

  /** Default threshold distance (in pixels) for detecting swipe gestures. */
  const val DefaultSwipeThreshold = 64f
}
