package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.android.sample.ui.calendar.components.DayHeaderRow
import com.android.sample.ui.calendar.components.EventsPane
import com.android.sample.ui.calendar.components.GridCanvas
import com.android.sample.ui.calendar.components.NowIndicatorLine
import com.android.sample.ui.calendar.components.TimeAxisColumn
import com.android.sample.ui.calendar.mockData.MockCalendarViewModel.Companion.getMockEvents
import com.android.sample.ui.calendar.mockData.MockEvent
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.calendar.utils.LocalDateRange
import com.android.sample.ui.calendar.utils.rememberWeekViewMetrics
import java.time.LocalTime
import kotlinx.coroutines.delay

@Composable
fun CalendarGridContent(
    modifier: Modifier = Modifier,
    dateRange: LocalDateRange = run {
      val today = java.time.LocalDate.now()
      val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
      val endOfWeek = today.with(java.time.DayOfWeek.FRIDAY)
      LocalDateRange(startOfWeek, endOfWeek)
    },
    events: List<MockEvent> = getMockEvents(),
    style: GridContentStyle = defaultGridContentStyle()
    // Later : receive onEventClick and onEventLongPress
) {
  val metrics = rememberWeekViewMetrics(dateRange, events)

  val scrollState = rememberScrollState()

  var now by remember { mutableStateOf(LocalTime.now()) }

  LaunchedEffect(Unit) {
    while (true) {
      now = LocalTime.now()
      delay(1000)
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val availableWidth = this.maxWidth - metrics.leftOffsetDp
    val dynamicColumnWidthDp =
        if (metrics.columnCount > 0) (availableWidth / metrics.columnCount) else availableWidth

    Column(modifier = Modifier.fillMaxSize()) {
      DayHeaderRow(
          days = metrics.days,
          leftOffsetDp = metrics.leftOffsetDp,
          topOffsetDp = metrics.topOffsetDp,
          columnWidth = dynamicColumnWidthDp)

      Row(modifier = Modifier.weight(1f)) {
        TimeAxisColumn(
            timeLabels = metrics.timeLabels,
            rowHeightDp = metrics.rowHeightDp,
            gridHeightDp = metrics.gridHeightDp,
            leftOffsetDp = metrics.leftOffsetDp,
            scrollState = scrollState,
            style = style
            // Later : scrollState = scrollState,
            )

        // for now :            Grid Area (Canvas + Events)
        // Later :   Scrollable Grid Area (Canvas + Events)
        Box(
            modifier = Modifier.verticalScroll(scrollState).weight(1f).height(metrics.gridHeightDp),
        ) {
          // Render the grid background
          GridCanvas(
              modifier = Modifier.fillMaxSize(),
              columnCount = metrics.columnCount,
              rowHeightDp = metrics.rowHeightDp,
              totalHours = metrics.totalHours.toInt(),
              days = metrics.days,
              style = style)

          // Render all the events blocks
          EventsPane(
              days = metrics.days,
              events = events,
              columnWidthDp = dynamicColumnWidthDp,
              gridHeightDp = metrics.gridHeightDp,
              gridStartTime = metrics.gridStartTime,
              effectiveEndTime = metrics.effectiveEndTime,
          )

          // Render the "now" indicator line
          NowIndicatorLine(
              modifier = Modifier.fillMaxSize(),
              columnCount = metrics.columnCount,
              rowHeightDp = metrics.rowHeightDp,
              days = metrics.days,
              now = now,
              gridStartTime = metrics.gridStartTime,
              effectiveEndTime = metrics.effectiveEndTime,
              style = style)
        }
      }
    }
  }
}
