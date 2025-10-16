package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.mockData.MockEvent
import com.android.sample.ui.calendar.style.CalendarDefaults

/**
 * High-level container for the calendar screen. It hosts the grid background, the events layer, and
 * (later) swipe/zoom behaviors. Use this as the entry point to render a week view.
 *
 * @param modifier [Modifier] applied to the root container.
 * @param dateRange Visible date range (inclusive) to render.
 * @param events List of events to display in the grid.
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
fun CalendarContainer(
    modifier: Modifier = Modifier,
    dateRange: LocalDateRange = CalendarDefaults.DefaultDateRange,
    events: List<MockEvent> = listOf()
    // Later : receive here the ViewModel (or the uiState to add/get/delete)
    // Later : receive here onEventClick, onEventLongPress, onSwipeLeft, onSwipeRight
) {
  // Later : create here a variable transformableState for zoom changes
  // Later : handle here variables for animation of swiping (transparent box)

  Box(
      modifier = modifier
      // Later : add modifier to handle swiping
      ) {
        CalendarGridContent(
            modifier = Modifier.fillMaxSize(), dateRange = dateRange, events = events
            // Later : give dateRange (like Monday-Friday) and events list from ViewModel
            // Later : give onEventClick and onEventLongPress
            )

        // Later : manage visual swiping effects here
      }
}
