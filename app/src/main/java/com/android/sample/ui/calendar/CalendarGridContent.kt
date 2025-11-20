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
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay

/**
 * Core layout of the week-view grid area: time axis, scrollable grid canvas, events overlay, and a
 * live "now" indicator. Also manages a 1s ticker to keep the current time line in sync.
 *
 * @param modifier [Modifier] applied to the whole grid content.
 * @param dateRange Visible date range used to compute layout metrics (column count, labels, etc.).
 * @param headerDateRange Date range used only for the day header row. In ONE_DAY mode it can be a
 *   full week that contains the visible day.
 * @param events List of events to render within the visible range.
 * @param style Visual style (colors, spacing, dimensions) for the grid and labels.
 * @param today The current calendar day. Used to highlight "today" in the header.
 * @param selectedDate Optional selected date in the header (for example the single visible day in
 *   ONE_DAY mode).
 * @param onHeaderDayClick Optional callback when the user taps a day in the header. If this is
 *   null, the header is not clickable.
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
fun CalendarGridContent(
    modifier: Modifier = Modifier,
    dateRange: LocalDateRange = CalendarDefaults.DefaultDateRange,
    headerDateRange: LocalDateRange = dateRange,
    events: List<Event> = listOf(),
    style: GridContentStyle = defaultGridContentStyle(),
    today: LocalDate = LocalDate.now(),
    selectedDate: LocalDate? = null,
    onHeaderDayClick: ((LocalDate) -> Unit)? = null,
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

  // Compute the list of days shown in the header.
  // In ONE_DAY mode, this will typically be the full week containing the visible day.
  val headerDays = headerDateRange.toDateList()

  BoxWithConstraints(modifier = modifier.fillMaxSize().testTag(CalendarScreenTestTags.ROOT)) {
    val availableWidth = this.maxWidth - metrics.leftOffsetDp

    // Column width for the grid (events + background). It is based on the visible date range.
    val gridColumnWidthDp =
        if (metrics.columnCount > 0) (availableWidth / metrics.columnCount) else availableWidth

    // Column width for the header. It is based on the number of header days.
    // In ONE_DAY mode, this allows us to show a full week with multiple columns.
    val headerColumnWidthDp =
        if (headerDays.isNotEmpty()) (availableWidth / headerDays.size) else availableWidth

    Column(modifier = Modifier.fillMaxSize()) {
      DayHeaderRow(
          days = headerDays,
          leftOffsetDp = metrics.leftOffsetDp,
          topOffsetDp = metrics.topOffsetDp,
          columnWidth = headerColumnWidthDp,
          style = style,
          today = today,
          selectedDate = selectedDate,
          onDayClick = onHeaderDayClick)

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
                  days = metrics.days,
                  today = today,
                  selectedDate = selectedDate)

              // Render all the events blocks
              EventsPane(
                  days = metrics.days,
                  events = events,
                  columnWidthDp = gridColumnWidthDp,
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

/**
 * Helper to turn a [LocalDateRange] into a list of [LocalDate] from start to end (inclusive).
 *
 * This is used by the header to iterate over all visible days.
 */
private fun LocalDateRange.toDateList(): List<LocalDate> {
  val result = mutableListOf<LocalDate>()
  var current = start
  while (!current.isAfter(endInclusive)) {
    result.add(current)
    current = current.plusDays(1)
  }
  return result
}
