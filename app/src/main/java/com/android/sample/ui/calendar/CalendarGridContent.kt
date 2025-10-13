package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.calendar.components.GridCanvas
import com.android.sample.ui.calendar.components.NowIndicatorLine
import com.android.sample.ui.calendar.components.TimeAxisColumn
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.calendar.utils.LocalDateRange
import com.android.sample.ui.calendar.utils.rememberWeekViewMetrics
import com.android.sample.ui.calendar.utils.workWeekDays
import java.time.LocalTime

@Composable
fun CalendarGridContent(
    modifier: Modifier = Modifier,
    style: GridContentStyle = defaultGridContentStyle()
    // todo : receive a date range
    // todo : receive events (or take an empty list)
    // Later : receive onEventClick and onEventLongPress
) {

  // todo : use a "metrics" helper to handle placement easily

  val scrollState = rememberScrollState()
  var now by remember { mutableStateOf(LocalTime.now()) }

  val days = workWeekDays()
  val range = LocalDateRange(days.first(), days.last())
  val metrics = rememberWeekViewMetrics(dateRange = range)

  // todo : create a “now“ variable to show the current time

  // todo : change the box into a BoxWithConstraints to handle componnents placement for eventsPane
  // and dayHeaderRow
  Box(modifier = modifier.fillMaxSize().testTag(CalendarScreenTestTags.ROOT)) {
    // todo : create variable to handle component placement correctly

    Column(modifier = Modifier.fillMaxSize()) {
      // todo : render a DayHeaderRow

      Row(modifier = Modifier.weight(1f).testTag(CalendarScreenTestTags.SCROLL_AREA)) {
        TimeAxisColumn(
            timeLabels = metrics.timeLabels,
            rowHeightDp = metrics.rowHeightDp,
            gridHeightDp = metrics.gridHeightDp,
            leftOffsetDp = metrics.leftOffsetDp,
            scrollState = scrollState,
            style = style)

        // Scrollable Grid Area (Canvas + Events)
        Box(
            modifier = Modifier.verticalScroll(scrollState).weight(1f) // todo : adapt the height
            ) {
              GridCanvas(
                  modifier = Modifier.fillMaxSize(),
                  columnCount = metrics.columnCount,
                  rowHeightDp = metrics.rowHeightDp,
                  totalHours = metrics.totalHours,
                  days = metrics.days)

              // todo : render an EventsPane

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
