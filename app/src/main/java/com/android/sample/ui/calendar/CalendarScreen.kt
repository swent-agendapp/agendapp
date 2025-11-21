package com.android.sample.ui.calendar

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultDateRange
import com.android.sample.ui.calendar.utils.DateTimeUtils.localDateTimeToInstant
import com.android.sample.ui.common.FloatingButton
import com.android.sample.ui.common.MainPageTopBar
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import java.time.LocalTime

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
fun CalendarScreen(
    calendarViewModel: CalendarViewModel = viewModel(),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
    onCreateEvent: () -> Unit = {},
    onEventClick: (Event) -> Unit = {}
) {
  // initialize the week from monday to friday
  var currentDateRange by remember { mutableStateOf(DefaultDateRange) }

  val context = LocalContext.current
  val uiState by calendarViewModel.uiState.collectAsState()
  val events = uiState.events
  val selectedOrgId by selectedOrganizationViewModel.selectedOrganizationId.collectAsState()

  // Fetch events when the screen is recomposed
  LaunchedEffect(currentDateRange, selectedOrgId) {
    if (selectedOrgId != null) {
      loadEventsForDateRange(calendarViewModel, currentDateRange)
    }
  }

  // Show error message if fetching events fails
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      calendarViewModel.clearErrorMsg()
    }
  }
  Scaffold(
      topBar = {
        MainPageTopBar(
            title = stringResource(R.string.calendar_screen_title),
            modifier = Modifier.testTag(CalendarScreenTestTags.TOP_BAR_TITLE))
      },
      floatingActionButton = {
        FloatingButton(
            modifier = Modifier.testTag(ADD_EVENT_BUTTON),
            onClick = onCreateEvent,
            icon = Icons.Default.Add)
      }) { paddingValues ->
        // Later : place CalendarContainer in a Column to add button above/under
        CalendarContainer(
            modifier =
                Modifier.padding(paddingValues)
                    .fillMaxSize()
                    .testTag((CalendarScreenTestTags.SCREEN_ROOT)),
            dateRange = currentDateRange,
            events = events,
            onSwipeLeft = {
              val nextStart = currentDateRange.start.plusWeeks(1)
              val nextEnd = currentDateRange.endInclusive.plusWeeks(1)
              currentDateRange = LocalDateRange(nextStart, nextEnd)
            },
            onSwipeRight = {
              val nextStart = currentDateRange.start.minusWeeks(1)
              val nextEnd = currentDateRange.endInclusive.minusWeeks(1)
              currentDateRange = LocalDateRange(nextStart, nextEnd)
            },
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

@Preview
@Composable
fun CalendarScreenPreview() {
  CalendarScreen()
}
