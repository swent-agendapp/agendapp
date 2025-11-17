package com.android.sample.ui.calendar

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.utils.DateTimeUtils.localDateTimeToInstant
import java.time.LocalTime

/**
 * High-level container for the calendar screen. It hosts the grid background, the events layer, and
 * (later) swipe/zoom behaviors. Use this as the entry point to render a week view.
 *
 * This composable owns the visible [LocalDateRange], handles week-to-week navigation on horizontal
 * swipes, and triggers loading of events from [CalendarViewModel] whenever the range changes.
 *
 * @param modifier [Modifier] applied to the root container.
 * @param calendarViewModel ViewModel used to fetch events for the visible date range.
 * @param onEventClick Callback invoked when the user taps on an [Event].
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
fun CalendarContainer(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel = viewModel(),
    onEventClick: (Event) -> Unit = {}
) {
  // Later : create here a variable transformableState for zoom changes
  // Later : handle here variables for animation of swiping (transparent box)

  // Initialize the week from Monday to Friday
  var currentDateRange by remember { mutableStateOf(CalendarDefaults.DefaultDateRange) }

  // Observe events from the view model
  val uiState by calendarViewModel.uiState.collectAsState()
  val events = uiState.events

  // Fetch events whenever the visible date range changes
  LaunchedEffect(currentDateRange) { loadEventsForDateRange(calendarViewModel, currentDateRange) }

  Box(modifier = modifier) {
    CalendarGridContent(
        modifier =
            Modifier.fillMaxSize().pointerInput(currentDateRange) {
              var totalDx = 0f

              detectDragGestures(
                  onDrag = { _, dragAmount -> totalDx += dragAmount.x },
                  onDragEnd = {
                    val threshold = CalendarDefaults.DefaultSwipeThreshold
                    when {
                      // Swipe right: go to previous week
                      totalDx > threshold -> {
                        val nextStart = currentDateRange.start.minusWeeks(1)
                        val nextEnd = currentDateRange.endInclusive.minusWeeks(1)
                        currentDateRange = LocalDateRange(nextStart, nextEnd)
                      }
                      // Swipe left: go to next week
                      totalDx < -threshold -> {
                        val nextStart = currentDateRange.start.plusWeeks(1)
                        val nextEnd = currentDateRange.endInclusive.plusWeeks(1)
                        currentDateRange = LocalDateRange(nextStart, nextEnd)
                      }
                    }
                    totalDx = 0f
                  },
                  onDragCancel = { totalDx = 0f })
            },
        dateRange = currentDateRange,
        events = events,
        onEventClick = onEventClick)
  }
}

/**
 * Loads the calendar events for a given date range using the provided [CalendarViewModel].
 *
 * Converts the [LocalDateRange] into corresponding [java.time.Instant] values covering the full
 * duration from start of the first day (midnight) to the end of the last day.
 */
private fun loadEventsForDateRange(
    calendarViewModel: CalendarViewModel,
    dateRange: LocalDateRange
) {
  calendarViewModel.loadEventsBetween(
      localDateTimeToInstant(dateRange.start, LocalTime.MIDNIGHT),
      localDateTimeToInstant(dateRange.endInclusive, LocalTime.MAX))
}
