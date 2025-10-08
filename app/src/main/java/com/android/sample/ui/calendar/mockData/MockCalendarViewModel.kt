package com.android.sample.ui.calendar.mockData

import com.android.sample.ui.calendar.utils.TimeSpan
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class MockCalendarViewModel {

  companion object {
    fun getMockEvents(): List<MockEvent> {
      return listOf(
          // WEEK 0 - event 1
          MockEvent(
              date = LocalDate.of(2025, 10, 6),
              title = "First event",
              timeSpan = TimeSpan.of(start = LocalTime.of(9, 30), duration = Duration.ofHours(2)),
              assigneeText = "Emilien",
              backgroundColor = 0xFFFFB74D.toInt()),

          // WEEK 0 - event 2
          MockEvent(
              date = LocalDate.of(2025, 10, 7),
              title = "Nice event",
              timeSpan = TimeSpan.of(start = LocalTime.of(14, 0), duration = Duration.ofHours(4)),
              assigneeText = "Méline",
              backgroundColor = 0xFF81C784.toInt()),

          // WEEK 0 - event 3
          MockEvent(
              date = LocalDate.of(2025, 10, 8),
              title = "Top Event",
              timeSpan = TimeSpan.of(start = LocalTime.of(11, 0), duration = Duration.ofHours(2)),
              assigneeText = "Nathan",
              backgroundColor = 0xFF64B5F6.toInt()),

          // WEEK +1 - event 1
          MockEvent(
              date = LocalDate.of(2025, 10, 13),
              title = "Next Event",
              timeSpan = TimeSpan.of(start = LocalTime.of(10, 0), duration = Duration.ofHours(3)),
              assigneeText = "Noa",
              backgroundColor = 0xFFBA68C8.toInt()),

          // WEEK +1 - event 2
          MockEvent(
              date = LocalDate.of(2025, 10, 16),
              title = "Later Event",
              timeSpan = TimeSpan.of(start = LocalTime.of(16, 0), duration = Duration.ofHours(4)),
              assigneeText = "Timaël",
              backgroundColor = 0xFF4DB6AC.toInt()),

          // WEEK -1 - event 1
          MockEvent(
              date = LocalDate.of(2025, 9, 30),
              title = "Previous Event",
              timeSpan = TimeSpan.of(start = LocalTime.of(17, 0), duration = Duration.ofHours(2)),
              assigneeText = "Haobin",
              backgroundColor = 0xFFFFB74D.toInt()),

          // WEEK -1 - event 2
          MockEvent(
              date = LocalDate.of(2025, 10, 2),
              title = "Earlier Event",
              timeSpan = TimeSpan.of(start = LocalTime.of(8, 0), duration = Duration.ofHours(4)),
              assigneeText = "Weifeng",
              backgroundColor = 0xFF5C6BC0.toInt()))
    }
  }
}
