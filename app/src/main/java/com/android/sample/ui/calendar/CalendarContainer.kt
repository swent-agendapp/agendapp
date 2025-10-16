package com.android.sample.ui.calendar

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    events: List<MockEvent> = listOf(),
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null
    // Later : receive here the ViewModel (or the uiState to add/get/delete)
    // Later : receive here onEventClick, onEventLongPress
) {
  // Later : create here a variable transformableState for zoom changes

  Box(modifier = modifier) {
    CalendarGridContent(
        modifier =
            Modifier.fillMaxSize().pointerInput(onSwipeLeft, onSwipeRight) {
              var totalDx = 0f
              detectDragGestures(
                  onDrag = { change, dragAmount -> totalDx += dragAmount.x },
                  onDragEnd = {
                    val threshold = 64f
                    when {
                      totalDx > threshold -> onSwipeRight?.invoke()
                      totalDx < -threshold -> onSwipeLeft?.invoke()
                    }
                    totalDx = 0f
                  },
                  onDragCancel = { totalDx = 0f })
            },
        dateRange = dateRange,
        events = events
        // Later : give dateRange (like Monday-Friday) and events list from ViewModel
        // Later : give onEventClick and onEventLongPress
        )
  }
  Box(
      modifier = modifier
      // Later : add modifier to handle swiping
      ) {
        CalendarGridContent(
            modifier = Modifier.fillMaxSize(), dateRange = dateRange, events = events
            // Later : give dateRange (like Monday-Friday) and events list from ViewModel
            // Later : give onEventClick and onEventLongPress
            )
        IconButton(
            onClick = {},
            modifier =
                Modifier.align(Alignment.TopStart).padding(top = 5.dp, start = 8.dp).size(35.dp)) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Gray)
            }

        // Later : manage visual swiping effects here
      }
}
