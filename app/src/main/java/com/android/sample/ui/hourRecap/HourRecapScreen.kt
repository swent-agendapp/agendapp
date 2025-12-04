package com.android.sample.ui.hourRecap

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.CalendarViewModel
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.utils.formatDecimalHoursToTime
import com.android.sample.ui.common.Loading
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.ElevationLow
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.Weight
import java.time.LocalDate
import java.time.ZoneOffset

// Modularization assisted by AI

/** Test tags for HourRecapScreen UI tests. */
object HourRecapTestTags {
  const val BACK_BUTTON = "hour_recap_back_button"
  const val SCREEN_ROOT = "hour_recap_screen_root"
  const val TOP_BAR = "hour_recap_top_bar"
  const val START_DATE = "hour_recap_start_date"
  const val END_DATE = "hour_recap_end_date"
  const val GENERATE_BUTTON = "hour_recap_generate_button"
  const val RECAP_LIST = "hour_recap_list"
  const val RECAP_ITEM = "hour_recap_item"
  const val EXPORT_BUTTON = "hour_recap_export_button"
}

/**
 * HourRecapScreen
 *
 * A screen allowing an administrator to select a start and end date and visualize the total working
 * hours of all employees over the selected period.
 *
 * This screen currently uses fake recap data; the real implementation will later delegate the
 * date-range selection and recap generation to a dedicated ViewModel.
 *
 * UI structure:
 * - A top bar with a back button and an "Export to Excel" action button.
 * - Two date picker fields (start / end).
 * - A "Generate recap" button (enabled only when both dates are selected).
 * - A scrollable list summarizing each employee's worked hours.
 *
 * Test tags are provided through [HourRecapTestTags] to support UI testing.
 *
 * @param onBackClick Callback invoked when the user presses the back navigation button.
 */
@Composable
fun HourRecapScreen(
    calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory),
    onBackClick: () -> Unit = {}
) {
  val uiState by calendarViewModel.uiState.collectAsState()
  val context = LocalContext.current

  // ---- ERROR HANDLING (Project standard) ----
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      calendarViewModel.clearErrorMsg()
    }
  }

  var startDate by remember { mutableStateOf<LocalDate?>(null) }
  var endDate by remember { mutableStateOf<LocalDate?>(null) }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            modifier = Modifier.testTag(HourRecapTestTags.TOP_BAR),
            title = stringResource(R.string.hour_recap_title),
            canGoBack = true,
            onClick = onBackClick,
            backButtonTestTags = HourRecapTestTags.BACK_BUTTON,
            actions = {
              IconButton(
                  onClick = { /* Later: Export */},
                  modifier = Modifier.testTag(HourRecapTestTags.EXPORT_BUTTON)) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = stringResource(R.string.export_excel))
                  }
            })
      }) { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .padding(SpacingLarge)
                    .fillMaxSize()
                    .testTag(HourRecapTestTags.SCREEN_ROOT),
            verticalArrangement = Arrangement.Top) {

              // ---- Start date Picker ----
              DatePickerFieldToModal(
                  label = stringResource(R.string.startDatePickerLabel),
                  modifier = Modifier.fillMaxWidth().testTag(HourRecapTestTags.START_DATE),
                  onDateSelected = { startDate = it })

              Spacer(Modifier.height(SpacingMedium))

              // ---- End date Picker ----
              DatePickerFieldToModal(
                  label = stringResource(R.string.endDatePickerLabel),
                  modifier = Modifier.fillMaxWidth().testTag(HourRecapTestTags.END_DATE),
                  onDateSelected = { endDate = it })

              Spacer(Modifier.height(SpacingMedium))

              // ---- Generate Recap Button ----
              PrimaryButton(
                  modifier = Modifier.fillMaxWidth().testTag(HourRecapTestTags.GENERATE_BUTTON),
                  text = stringResource(R.string.hour_recap_generate),
                  enabled = startDate != null && endDate != null && !startDate!!.isAfter(endDate!!),
                  onClick = {
                    calendarViewModel.calculateWorkedHours(
                        start = startDate!!.atStartOfDay().toInstant(ZoneOffset.UTC),
                        end = endDate!!.atTime(23, 59).toInstant(ZoneOffset.UTC))
                  })

              Spacer(Modifier.height(SpacingLarge))

              // ---- Title ----
              Text(
                  text = stringResource(R.string.hour_recap_results_title),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

              Spacer(Modifier.height(SpacingMedium))

              // ---- LOADING ----
              if (uiState.isLoading) {
                Loading(
                    modifier = Modifier.fillMaxSize(),
                    label = stringResource(R.string.loading_hours))
              } else {
                // ---- LIST OF WORKED HOURS ----
                LazyColumn(modifier = Modifier.testTag(HourRecapTestTags.RECAP_LIST)) {
                  items(uiState.workedHours) { pair ->
                    val (name, hours) = pair

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(vertical = PaddingSmall)
                                .testTag(HourRecapTestTags.RECAP_ITEM),
                        elevation = CardDefaults.cardElevation(ElevationLow)) {
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(Weight))
                                Text(
                                    text = formatDecimalHoursToTime(hours),
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold))
                              }
                        }
                  }
                }
              }
            }
      }
}
