package com.android.sample.ui.calendar

import android.content.res.Configuration
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.Event
import com.android.sample.ui.common.FloatingButton
import com.android.sample.ui.common.MainPageTopBar

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
    onCreateEvent: () -> Unit = {},
    onEventClick: (Event) -> Unit = {}
) {
  val context = LocalContext.current
  val uiState by calendarViewModel.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // Show error message if fetching events fails
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      calendarViewModel.clearErrorMsg()
    }
  }

  Scaffold(
      topBar = {
          if (isPortrait) {
            MainPageTopBar(
                title = stringResource(R.string.calendar_screen_title),
                modifier = Modifier.testTag(CalendarScreenTestTags.TOP_BAR_TITLE))
          }
      },
      floatingActionButton = {
        FloatingButton(
            modifier = Modifier.testTag(CalendarScreenTestTags.ADD_EVENT_BUTTON),
            onClick = onCreateEvent,
            icon = Icons.Default.Add)
      }) { paddingValues ->
        CalendarContainer(
            modifier =
                Modifier.padding(paddingValues)
                    .fillMaxSize()
                    .testTag(CalendarScreenTestTags.SCREEN_ROOT),
            calendarViewModel = calendarViewModel,
            onEventClick = onEventClick)
      }
}

@Preview
@Composable
fun CalendarScreenPreview() {
  CalendarScreen()
}
