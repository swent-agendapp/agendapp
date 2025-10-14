package com.android.sample.ui.calendar.mockData

import com.android.sample.ui.calendar.data.TimeSpan
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class MockEvent(
    val date: LocalDate,
    val title: String,
    val timeSpan: TimeSpan,
    val assigneeName: String,
    val backgroundColor: Int
)

fun getMockEvents(): List<MockEvent> {
  return listOf(
      // WEEK 0 - event 1
      MockEvent(
          date = LocalDate.of(2025, 10, 14),
          title = "First event",
          timeSpan = TimeSpan.of(start = LocalTime.of(9, 30), duration = Duration.ofHours(2)),
          assigneeName = "Emilien",
          backgroundColor = 0xFFFFB74D.toInt()),

      // WEEK 0 - event 2
      MockEvent(
          date = LocalDate.of(2025, 10, 15),
          title = "Nice event",
          timeSpan = TimeSpan.of(start = LocalTime.of(14, 0), duration = Duration.ofHours(4)),
          assigneeName = "Méline",
          backgroundColor = 0xFF81C784.toInt()),

      // WEEK 0 - event 3
      MockEvent(
          date = LocalDate.of(2025, 10, 16),
          title = "Top Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(11, 0), duration = Duration.ofHours(2)),
          assigneeName = "Nathan",
          backgroundColor = 0xFF64B5F6.toInt()),

      // WEEK +1 - event 1
      MockEvent(
          date = LocalDate.of(2025, 10, 20),
          title = "Next Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(10, 0), duration = Duration.ofHours(3)),
          assigneeName = "Noa",
          backgroundColor = 0xFFBA68C8.toInt()),

      // WEEK +1 - event 2
      MockEvent(
          date = LocalDate.of(2025, 10, 23),
          title = "Later Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(16, 0), duration = Duration.ofHours(4)),
          assigneeName = "Timaël",
          backgroundColor = 0xFF4DB6AC.toInt()),

      // WEEK -1 - event 1
      MockEvent(
          date = LocalDate.of(2025, 10, 7),
          title = "Previous Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(17, 0), duration = Duration.ofHours(2)),
          assigneeName = "Haobin",
          backgroundColor = 0xFFFFB74D.toInt()),

      // WEEK -1 - event 2
      MockEvent(
          date = LocalDate.of(2025, 10, 9),
          title = "Earlier Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(8, 0), duration = Duration.ofHours(4)),
          assigneeName = "Weifeng",
          backgroundColor = 0xFF5C6BC0.toInt()))
}
