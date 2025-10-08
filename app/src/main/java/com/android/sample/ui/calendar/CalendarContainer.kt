package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.sample.ui.calendar.mockData.MockEvent
import com.android.sample.ui.calendar.utils.LocalDateRange

@Composable
fun CalendarContainer(
    modifier: Modifier = Modifier,
    dateRange: LocalDateRange,
    events: List<MockEvent>
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
            modifier = Modifier.fillMaxSize(),
            dateRange = dateRange,
            // for now :
            events = events
            // Later : give dateRange (like Monday-Friday) and events list from ViewModel
            // Later : give onEventClick and onEventLongPress
            )

        // Later : manage visual swiping effects here
      }
}
