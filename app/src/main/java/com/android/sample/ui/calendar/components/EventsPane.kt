package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.data.workWeekDays
import com.android.sample.ui.calendar.mockData.MockEvent
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun EventsPane(
    days: List<LocalDate> = workWeekDays(),
    events: List<MockEvent> = listOf(),
    columnWidthDp: Dp = defaultGridContentStyle().dimensions.defaultColumnWidthDp,
    gridHeightDp: Dp = defaultGridContentStyle().dimensions.rowHeightDp,
    gridStartTime: LocalTime = CalendarDefaults.DefaultStartTime,
    effectiveEndTime: LocalTime = CalendarDefaults.DefaultEndTime,
) {
  days.forEachIndexed { dayIndex, date ->
    val eventsForDay = events.filter { it.date == date }
    if (eventsForDay.isNotEmpty()) {
      Box(
          modifier =
              Modifier.offset(x = dayIndex * columnWidthDp)
                  .size(columnWidthDp, gridHeightDp)
                  .testTag(CalendarScreenTestTags.EVENT_GRID)) {
            // for now (later : EventBlockWithOverlapHandling)
            EventBlock(
                events = eventsForDay,
                startTime = gridStartTime,
                endTime = effectiveEndTime,
                columnWidthDp = columnWidthDp)
          }
    }
  }
}
