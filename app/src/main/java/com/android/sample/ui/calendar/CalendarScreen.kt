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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.calendar.mockData.getMockEvents
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultDateRange

object CalendarScreenTestTags {
  const val TOP_BAR_TITLE = "CalendarTopBarTitle"
  const val SCREEN_ROOT = "CalendarScreenRoot"
  const val SCROLL_AREA = "CalendarGridScrollArea"
  const val ROOT = "CalendarGridRoot"
  const val EVENT_GRID = "CalendarEventGrid"
  const val TIME_AXIS_COLUMN = "TimeAxisColumn"
  const val NOW_INDICATOR = "NowIndicator"
  const val EVENT_BLOCK = "CalendarEventBlock"
  const val DAY_ROW = "CalendarDayRow"
  const val ADD_EVENT_BUTTON = "AddEventButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onCreateEvent: () -> Unit = {}) {
  // initialize the week from monday to friday
  val currentDateRange by remember { mutableStateOf(DefaultDateRange) }

  // generate mock events
  val mockEvents = getMockEvents()

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
        modifier =
            Modifier.padding(paddingValues)
                .fillMaxSize()
                .testTag((CalendarScreenTestTags.SCREEN_ROOT)),
        dateRange = currentDateRange,
        events = mockEvents,
        onCreateEvent = onCreateEvent
        // Later : give the ViewModel
        // Later : add here onEventClick, onEventLongPress, onSwipeLeft, onSwipeRight
        )
  }
}
