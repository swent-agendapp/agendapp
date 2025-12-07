package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.data.workWeekDays
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.calendar.utils.DateTimeUtils
import java.time.LocalDate
import java.time.LocalTime

/**
 * Renders all event blocks for the provided list of days. Each day occupies a column; events are
 * filtered by day and positioned vertically according to their time span.
 *
 * @param days Ordered list of days (columns) to display.
 * @param events All events across the given days. They will be filtered per day.
 * @param columnWidthDp The width of each day column, in [Dp].
 * @param gridHeightDp Total scrollable grid height, in [Dp].
 * @param gridStartTime Inclusive start time of the visible grid.
 * @param effectiveEndTime Exclusive end time of the visible grid used for layout.
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
fun EventsPane(
    days: List<LocalDate> = workWeekDays(),
    events: List<Event> = listOf(),
    columnWidthDp: Dp = defaultGridContentStyle().dimensions.defaultColumnWidthDp,
    gridHeightDp: Dp = defaultGridContentStyle().dimensions.rowHeightDp,
    gridStartTime: LocalTime = CalendarDefaults.DefaultStartTime,
    effectiveEndTime: LocalTime = CalendarDefaults.DefaultEndTime,
    selectedEvent: Event? = null,
    onEventClick: (Event) -> Unit = {}
) {
  days.forEachIndexed { dayIndex, date ->
    val dayStart = DateTimeUtils.dayStartInstant(date)
    val dayEndExclusive =
        DateTimeUtils.dayEndInstantExclusive(
            date) // do not accept event of the day before finishing at 00:00
    val eventsForDay = events.filter { it.endDate > dayStart && it.startDate < dayEndExclusive }
    if (eventsForDay.isNotEmpty()) {
      Box(
          modifier =
              Modifier.offset(x = dayIndex * columnWidthDp).size(columnWidthDp, gridHeightDp)) {
            // for now (later : EventBlockWithOverlapHandling)
            EventBlock(
                events = eventsForDay,
                currentDate = date,
                startTime = gridStartTime,
                endTime = effectiveEndTime,
                columnWidthDp = columnWidthDp,
                selectedEvent = selectedEvent,
                onEventClick = onEventClick)
          }
    }
  }
}
