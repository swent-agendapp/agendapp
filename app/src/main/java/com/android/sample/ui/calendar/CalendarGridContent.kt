package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun CalendarGridContent(
    modifier: Modifier = Modifier,
    // todo : receive a date range
    // todo : receive events (or take an empty list)
    // todo : receive style (or take default one)
    // Later : receive onEventClick and onEventLongPress
) {
  // todo : use a "metrics" helper to handle placement easily

  val scrollState = rememberScrollState()

  // todo : create a “now“ variable to show the current time

  // todo : change the box into a BoxWithConstraints to handle componnents placement for eventsPane
  // and dayHeaderRow
  Box(modifier = modifier.fillMaxSize().testTag(CalendarScreenTestTags.ROOT)) {
    // todo : create variable to handle component placement correctly

    Column(modifier = Modifier.fillMaxSize()) {
      // todo : render a DayHeaderRow

      Row(modifier = Modifier.weight(1f).testTag(CalendarScreenTestTags.SCROLL_AREA)) {
        // todo : render a TimeAxisColumn

        // Scrollable Grid Area (Canvas + Events)
        Box(
            modifier = Modifier.verticalScroll(scrollState).weight(1f) // todo : adapt the height
            ) {
              // todo : render a GridCanvas as a background

              // todo : render an EventsPane

              // todo : render a NowIndicatorLine
            }
      }
    }
  }
}
