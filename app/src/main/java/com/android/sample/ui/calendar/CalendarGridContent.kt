package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.calendar.components.EventsPane
import com.android.sample.ui.calendar.components.GridCanvas
import com.android.sample.ui.calendar.components.NowIndicatorLine
import com.android.sample.ui.calendar.components.TimeAxisColumn
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.mockData.MockEvent
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.calendar.utils.rememberWeekViewMetrics
import java.time.LocalTime
import kotlinx.coroutines.delay

@Composable
fun CalendarGridContent(
    modifier: Modifier = Modifier,
    style: GridContentStyle = defaultGridContentStyle(),
    dateRange: LocalDateRange = CalendarDefaults.DefaultDateRange,
    events: List<MockEvent> = listOf()
    // Later : receive onEventClick and onEventLongPress
) {
  val metrics = rememberWeekViewMetrics(dateRange)

  val scrollState = rememberScrollState()

  var now by remember { mutableStateOf(LocalTime.now()) }
  LaunchedEffect(Unit) {
    while (true) {
      now = LocalTime.now()
      delay(1000)
    }
  }

  // and dayHeaderRow
  BoxWithConstraints(modifier = modifier.fillMaxSize().testTag(CalendarScreenTestTags.ROOT)) {
    val availableWidth = this.maxWidth - metrics.leftOffsetDp
    val dynamicColumnWidthDp =
        if (metrics.columnCount > 0) (availableWidth / metrics.columnCount) else availableWidth

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
              // Render the grid background
              GridCanvas(
                  modifier = Modifier.fillMaxSize(),
                  columnCount = metrics.columnCount,
                  rowHeightDp = metrics.rowHeightDp,
                  totalHours = metrics.totalHours,
                  days = metrics.days)

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
