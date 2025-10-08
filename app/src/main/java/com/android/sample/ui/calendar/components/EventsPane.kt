package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import com.android.sample.ui.calendar.mockData.MockEvent
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun EventsPane(
    days: List<LocalDate>,
    events: List<MockEvent>,
    columnWidthDp: Dp,
    gridHeightDp: Dp,
    gridStartTime: LocalTime,
    effectiveEndTime: LocalTime,
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
