package com.android.sample.ui.calendar.mockData

import com.android.sample.ui.calendar.data.TimeSpan
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Demo representation of a calendar event used for previews and sample data.
 *
 * @property date The calendar day this event belongs to.
 * @property title Display title for the event.
 * @property timeSpan Start/end time window of the event.
 * @property assigneeName Person associated with the event (sample-only).
 * @property backgroundColor ARGB color used to paint the event block.
 */
data class MockEvent(
    val date: LocalDate,
    val title: String,
    val timeSpan: TimeSpan,
    val assigneeName: String,
    val backgroundColor: Int
)

/** Helper to get a date in a week offset from current week, for a specific day of week. */
private fun weekDate(weekOffset: Long, dayOfWeek: DayOfWeek): LocalDate {
  val startOfThisWeek = LocalDate.now().with(DayOfWeek.MONDAY)
  return startOfThisWeek.plusWeeks(weekOffset).with(dayOfWeek)
}

/**
 * Returns a deterministic set of sample events spanning previous, current, and next weeks.
 *
 * @return A list of [MockEvent] for UI development and testing.
 */
fun getMockEvents(): List<MockEvent> {
  return listOf(
      // WEEK 0 - event 1
      MockEvent(
          date = weekDate(0, DayOfWeek.TUESDAY),
          title = "First event",
          timeSpan = TimeSpan.of(start = LocalTime.of(9, 30), duration = Duration.ofHours(2)),
          assigneeName = "Emilien",
          backgroundColor = 0xFFFFB74D.toInt()),

      // WEEK 0 - event 2
      MockEvent(
          date = weekDate(0, DayOfWeek.WEDNESDAY),
          title = "Nice event",
          timeSpan = TimeSpan.of(start = LocalTime.of(14, 0), duration = Duration.ofHours(4)),
          assigneeName = "Méline",
          backgroundColor = 0xFF81C784.toInt()),

      // WEEK 0 - event 3
      MockEvent(
          date = weekDate(0, DayOfWeek.THURSDAY),
          title = "Top Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(11, 0), duration = Duration.ofHours(2)),
          assigneeName = "Nathan",
          backgroundColor = 0xFF64B5F6.toInt()),

      // WEEK +1 - event 1
      MockEvent(
          date = weekDate(1, DayOfWeek.MONDAY),
          title = "Next Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(10, 0), duration = Duration.ofHours(3)),
          assigneeName = "Noa",
          backgroundColor = 0xFFBA68C8.toInt()),

      // WEEK +1 - event 2
      MockEvent(
          date = weekDate(1, DayOfWeek.THURSDAY),
          title = "Later Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(16, 0), duration = Duration.ofHours(4)),
          assigneeName = "Timaël",
          backgroundColor = 0xFF4DB6AC.toInt()),

      // WEEK -1 - event 1
      MockEvent(
          date = weekDate(-1, DayOfWeek.TUESDAY),
          title = "Previous Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(17, 0), duration = Duration.ofHours(2)),
          assigneeName = "Haobin",
          backgroundColor = 0xFFFFB74D.toInt()),

      // WEEK -1 - event 2
      MockEvent(
          date = weekDate(-1, DayOfWeek.FRIDAY),
          title = "Earlier Event",
          timeSpan = TimeSpan.of(start = LocalTime.of(8, 0), duration = Duration.ofHours(4)),
          assigneeName = "Weifeng",
          backgroundColor = 0xFF5C6BC0.toInt()))
}
