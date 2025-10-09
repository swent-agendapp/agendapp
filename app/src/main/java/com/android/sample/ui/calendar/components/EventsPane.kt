package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import com.android.sample.ui.calendar.mockData.MockCalendarViewModel.Companion.getMockEvents
import com.android.sample.ui.calendar.mockData.MockEvent
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun EventsPane(
    days: List<LocalDate> = run {
      val today = LocalDate.now()
      val startOfWeek = today.with(DayOfWeek.MONDAY)
      val endOfWeek = today.with(DayOfWeek.FRIDAY)
      generateSequence(startOfWeek) { it.plusDays(1) }.takeWhile { it <= endOfWeek }.toList()
    },
    events: List<MockEvent> = getMockEvents(),
    columnWidthDp: Dp = defaultGridContentStyle().dimensions.defaultColumnWidthDp,
    gridHeightDp: Dp = defaultGridContentStyle().dimensions.rowHeightDp,
    gridStartTime: LocalTime = LocalTime.of(8, 0),
    effectiveEndTime: LocalTime = LocalTime.of(23, 0),
) {
  days.forEachIndexed { dayIndex, date ->
    val eventsForDay = events.filter { it.date == date }
    if (eventsForDay.isNotEmpty()) {
      Box(
          modifier =
              Modifier.offset(x = dayIndex * columnWidthDp).size(columnWidthDp, gridHeightDp)) {
            // for now : (later : EventBlockWithOverlapHandling)
            EventBlock(
                events = eventsForDay,
                startTime = gridStartTime,
                endTime = effectiveEndTime,
                columnWidthDp = columnWidthDp)
          }
    }
  }
}
