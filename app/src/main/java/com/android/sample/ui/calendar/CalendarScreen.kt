package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

object CalendarScreenTestTags {
  const val TOP_BAR_TITLE = "CalendarTopBarTitle"
  // todo : add tests tags (DAY_ROW, EVENT_GRID, TIME_AXIS_COLUMN)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
  // todo : create an initial date range (showing the current week)

  // todo : get mock events

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text("Calendar", modifier = Modifier.testTag(CalendarScreenTestTags.TOP_BAR_TITLE))
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        )
      },
  ) { paddingValues ->
    // Later : place CalendarContainer in a Column to add button above/under
    CalendarContainer(
        modifier = Modifier.padding(paddingValues).fillMaxSize(),
        // todo : give a dateRange
        // todo : give the events
        // Later : give the ViewModel
        // Later : add here onEventClick, onEventLongPress, onSwipeLeft, onSwipeRight
    )
  }
}
