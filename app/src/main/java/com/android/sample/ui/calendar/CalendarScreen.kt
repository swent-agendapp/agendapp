package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.components.DaysSegmentedControl
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.mockData.getMockEvents
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultDateRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

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

/**
 * Displays the main calendar screen composable.
 *
 * This composable defines the overall structure of the calendar UI, including:
 * - A top app bar with the title “Calendar”.
 * - A segmented control to switch between 1-day, 5-day, or 7-day views.
 * - A [CalendarContainer] displaying the grid, events, and swipe interactions.
 *
 * Behavior details:
 * - The selected day range determines how the header and grid are rendered.
 * - The week view always starts on Monday, while the 1-day view still shows the full week header.
 * - Horizontal swipes change the visible week or day accordingly.
 *
 * @param onCreateEvent Callback triggered when the user taps the “+” button to create a new event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onCreateEvent: () -> Unit = {}) {
  // initialize the week from monday to friday
  var gridDateRange by remember { mutableStateOf(DefaultDateRange) }
  var headerDateRange by remember { mutableStateOf(DefaultDateRange) }
  var selectedDays by remember { mutableIntStateOf(5) } // 1, 5 or 7

  // Computes the visible header range depending on the selected number of days
  fun computeHeaderRange(start: LocalDate, days: Int): LocalDateRange {
    return if (days == 1) {
      val monday = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
      val sunday = monday.plusDays(6)
      LocalDateRange(monday, sunday) // from Monday to Sunday for 1-day view
    } else {
      LocalDateRange(start, start.plusDays(days.toLong() - 1))
    }
  }

  // Generate placeholder events for UI preview
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
    Column(modifier = Modifier.padding(paddingValues)) {
      // Segmented control to choose between 1 day, 5 days or 7 days view
      DaysSegmentedControl(
          modifier =
              Modifier.align(Alignment.CenterHorizontally)
                  .padding(start = 12.dp, top = 12.dp, bottom = 8.dp, end = 12.dp)
                  .testTag("DaysSegmentedControl")
                  .fillMaxWidth(),
          selected = selectedDays,
          onSelect = { newDays ->
            // Update date ranges and recompute grid when the selected number of days changes
            if (newDays != selectedDays) {
              selectedDays = newDays
              val baseStart = gridDateRange.start
              val newGridStart =
                  if (newDays == 1) baseStart
                  else baseStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
              val newGridEnd = newGridStart.plusDays(newDays.toLong() - 1)
              gridDateRange = LocalDateRange(newGridStart, newGridEnd)
              headerDateRange = computeHeaderRange(newGridStart, newDays)
            }
          })

      CalendarContainer(
          modifier = Modifier.fillMaxSize().testTag((CalendarScreenTestTags.SCREEN_ROOT)),
          dateRange = gridDateRange,
          headerDateRange = headerDateRange,
          headerSingleDay = if (selectedDays == 1) gridDateRange.start else null,
          events = mockEvents,
          onSwipeLeft = {
            // Handle left/right swipe gestures to move between days or weeks
            val days = selectedDays
            val nextStart =
                if (days == 1) {
                  gridDateRange.start.plusDays(1)
                } else {
                  gridDateRange.start.plusWeeks(1)
                }
            val nextEnd = nextStart.plusDays(days.toLong() - 1)
            gridDateRange = LocalDateRange(nextStart, nextEnd)
            headerDateRange = computeHeaderRange(nextStart, days)
          },
          onSwipeRight = {
            // Handle left/right swipe gestures to move between days or weeks
            val days = selectedDays
            val nextStart =
                if (days == 1) {
                  gridDateRange.start.minusDays(1)
                } else {
                  gridDateRange.start.minusWeeks(1)
                }
            val nextEnd = nextStart.plusDays(days.toLong() - 1)
            gridDateRange = LocalDateRange(nextStart, nextEnd)
            headerDateRange = computeHeaderRange(nextStart, days)
          },
          onCreateEvent = onCreateEvent
          // Later : give the ViewModel
          // Later : add here onEventClick, onEventLongPress, onSwipeLeft, onSwipeRight
          )
    }
  }
}
