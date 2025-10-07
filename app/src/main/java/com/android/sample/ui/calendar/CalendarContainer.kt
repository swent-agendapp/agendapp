package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.sample.ui.calendar.mockData.MockEvent

@Composable
fun CalendarContainer(
    modifier: Modifier = Modifier,
    event: MockEvent
    // Later : receive here the ViewModel (or the weekData from the ViewModel)
    // Later : receive here onEventClick, onEventLongPress, onSwipeLeft, onSwipeRight
) {
    // Later : create here a variable transformableState for zoom changes
    // Later : handle here variables for animation of swiping (transparent box)

    Box(
        // Later : add modifier to handle swiping
    ) {
        CalendarGridContent(
            modifier = Modifier.fillMaxSize(),
            // for now :
            event = event
            // Later : give dateRange (like Monday-Friday) and events list from ViewModel
            // Later : give onEventClick and onEventLongPress
        )

        // Later : manage visual swiping effects here
    }
}

