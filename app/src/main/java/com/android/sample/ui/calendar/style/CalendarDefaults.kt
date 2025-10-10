package com.android.sample.ui.calendar.style

import java.time.Duration
import java.time.LocalTime

data object CalendarDefaults {
  val DefaultStartTime: LocalTime = LocalTime.of(8, 0)
  val DefaultEndTime: LocalTime = LocalTime.of(23, 0)
  const val DefaultDaysInWeek: Int = 5
  val DefaultTotalHour = Duration.between(DefaultStartTime, DefaultEndTime).toHours().toInt()
}
