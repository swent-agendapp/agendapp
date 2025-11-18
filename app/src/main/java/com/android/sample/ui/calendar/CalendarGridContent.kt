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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.components.DayHeaderRow
import com.android.sample.ui.calendar.components.EventsPane
import com.android.sample.ui.calendar.components.GridCanvas
import com.android.sample.ui.calendar.components.NowIndicatorLine
import com.android.sample.ui.calendar.components.TimeAxisColumn
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.calendar.utils.rememberWeekViewMetrics
import java.time.LocalTime
import kotlinx.coroutines.delay

/**
 * Core layout of the week-view grid area: time axis, scrollable grid canvas, events overlay, and a
 * live "now" indicator. Also manages a 1s ticker to keep the current time line in sync.
 *
 * @param modifier [Modifier] applied to the whole grid content.
 * @param dateRange Visible date range used to compute layout metrics (column count, labels, etc.).
 * @param events List of events to render within the visible range.
 * @param style Visual style (colors, spacing, dimensions) for the grid and labels.
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
fun CalendarGridContent(
    modifier: Modifier = Modifier,
    dateRange: LocalDateRange = CalendarDefaults.DefaultDateRange,
    events: List<Event> = listOf(),
    style: GridContentStyle = defaultGridContentStyle(),
    onEventClick: (Event) -> Unit = {}
) {
  val metrics = rememberWeekViewMetrics(dateRange)

  val scrollState = rememberScrollState()

  val density = LocalDensity.current

  // Start the viewport at the default initialHour on first composition, while allowing full-day
  // scrolling.
  // This ensures that the day start at this default time (instead of midnight), avoiding
  // unnecessary scrolling to reach the relevant hours of the day.
  LaunchedEffect(Unit) {
    if (scrollState.value == 0) {
      val initialHour = CalendarDefaults.DEFAULT_INITIAL_HOUR
      val offsetPx = with(density) { (metrics.rowHeightDp * initialHour).roundToPx() }
      scrollState.scrollTo(offsetPx)
    }
  }

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
      DayHeaderRow(
          days = metrics.days,
          leftOffsetDp = metrics.leftOffsetDp,
          topOffsetDp = metrics.topOffsetDp,
          columnWidth = dynamicColumnWidthDp)

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
            modifier = Modifier.verticalScroll(scrollState).weight(1f) // later : adapt the height
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
                  onEventClick = onEventClick)

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
