package com.android.sample.ui.calendar

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.filters.FilterBottomSheet
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultDateRange
import com.android.sample.ui.common.FloatingButton
import com.android.sample.ui.common.MainPageTopBar
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import com.android.sample.ui.theme.Palette

object CalendarScreenTestTags {
  // Top-level calendar screen tags
  const val PULL_TO_REFRESH = "CalendarPullToRefresh"
  const val TOP_BAR_TITLE = "CalendarTopBarTitle"
  const val SCREEN_ROOT = "CalendarScreenRoot"

  // Grid & content tags
  const val SCROLL_AREA = "CalendarGridScrollArea"
  const val ROOT = "CalendarGridRoot"
  const val EVENT_GRID = "CalendarEventGrid"
  const val TIME_AXIS_COLUMN = "TimeAxisColumn"
  const val NOW_INDICATOR = "NowIndicator"
  const val EVENT_BLOCK = "CalendarEventBlock"

  // Day header / row tags
  const val DAY_ROW = "CalendarDayRow"
  const val DAY_HEADER_DAY_PREFIX = "CalendarContainer_DayHeaderDay_" // + index or date

  // Calendar container tags
  const val CALENDAR_GRID = "CalendarContainer_Grid"
  const val DATE_PICKER_MODAL = "CalendarContainer_DatePickerModal"

  // View mode selector tags (moved from ViewModeSelectorTags)
  const val VIEW_MODE_SELECTOR_BOX = "ViewModeSelector_Box"
  const val VIEW_MODE_SELECTOR_FAB_PREFIX = "ViewModeSelector_Fab_" // + currentMode.name
  const val VIEW_MODE_SELECTOR_ITEM_PREFIX = "ViewModeSelector_Item_" // + mode.name

  // FAB / actions
  const val ADD_EVENT_BUTTON = "AddEventButton"

  // Filtering
  const val FILTER_BUTTON = "CalendarFilterButton"
  const val FILTER_BOTTOM_SHEET = "CalendarFilterBottomSheet"

  // Location status chip
  const val LOCATION_STATUS_CHIP = "LocationStatusChip"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
    onCreateEvent: () -> Unit = {},
    onEventClick: (Event) -> Unit = {}
) {
  // initialize the week from monday to friday
  var currentDateRange by remember { mutableStateOf(DefaultDateRange) }

  val context = LocalContext.current
  val uiState by calendarViewModel.uiState.collectAsState()
  val selectedOrgId by selectedOrganizationViewModel.selectedOrganizationId.collectAsState()

  val configuration = LocalConfiguration.current
  val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

  var showFilterSheet by remember { mutableStateOf(false) }

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
        if (isPortrait) {
          MainPageTopBar(
              title = "",
              modifier = Modifier.testTag(CalendarScreenTestTags.TOP_BAR_TITLE),
              actions = {
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.testTag(CalendarScreenTestTags.FILTER_BUTTON)) {
                      Icon(
                          imageVector = Icons.Default.FilterList,
                          contentDescription = stringResource(R.string.filter))
                    }
              },
              pastille = {
                LocationStatusChip(
                    locationStatus = uiState.locationStatus,
                    onClick = { calendarViewModel.checkUserLocationStatus() })
              })
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
        if (showFilterSheet) {
          FilterBottomSheet(
              onDismiss = { showFilterSheet = false },
              onApply = { filters ->
                calendarViewModel.applyFilters(filters)
                showFilterSheet = false
              })
        }
      }
}

/**
 * Composable function to display a location status chip.
 *
 * The chip changes color based on the location status:
 * - Green: User is inside an area
 * - Red: User is outside all areas
 * - Grey: User hasn't granted location permission
 *
 * assisted by AI
 *
 * @param locationStatus The current location status.
 */
@Composable
fun LocationStatusChip(locationStatus: LocationStatus, onClick: () -> Unit = {}) {
  val chipColor =
      when (locationStatus) {
        LocationStatus.INSIDE_AREA -> Palette.DarkSeaGreen
        LocationStatus.OUTSIDE_AREA -> Palette.Firebrick
        LocationStatus.NO_PERMISSION -> Palette.LightGray
      }

  val chipText =
      when (locationStatus) {
        LocationStatus.INSIDE_AREA -> stringResource(R.string.location_status_inside)
        LocationStatus.OUTSIDE_AREA -> stringResource(R.string.location_status_outside)
        LocationStatus.NO_PERMISSION -> stringResource(R.string.location_status_no_permission)
      }

  AssistChip(
      onClick = { onClick() },
      label = { Text(text = chipText) },
      colors =
          AssistChipDefaults.assistChipColors(containerColor = chipColor, labelColor = Color.White),
      modifier = Modifier.testTag(CalendarScreenTestTags.LOCATION_STATUS_CHIP))
}

@Preview
@Composable
fun CalendarScreenPreview() {
  CalendarScreen()
}
