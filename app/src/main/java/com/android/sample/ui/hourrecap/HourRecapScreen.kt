package com.android.sample.ui.hourrecap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.theme.ElevationLow
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.Weight
import com.android.sample.ui.theme.heightMedium
import com.android.sample.ui.theme.heightSmall
import java.time.LocalDate

// Modularization assisted by AI
object HourRecapTestTags {
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourRecapScreen(onBackClick: () -> Unit = {}) {
  var startDate by remember { mutableStateOf<LocalDate?>(null) }
  var endDate by remember { mutableStateOf<LocalDate?>(null) }

  // Fake data for now — later replaced by ViewModel
  val fakeRecap: Map<String, String> =
      mapOf("Alice" to "12h 30m", "Bob" to "8h 00m", "Charlie" to "4h 45m")

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag(HourRecapTestTags.TOP_BAR),
            title = { Text(stringResource(R.string.hour_recap_title)) },
            navigationIcon = {
              IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.goBack))
              }
            },
            actions = {
              IconButton(
                  onClick = { /* Later: implement Excel export */},
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

              // Start Date
              DatePickerFieldToModal(
                  label = stringResource(R.string.startDatePickerLabel),
                  onDateSelected = { selected -> startDate = selected },
                  modifier = Modifier.fillMaxWidth().testTag(HourRecapTestTags.START_DATE))

              Spacer(Modifier.height(heightSmall))

              // End Date
              DatePickerFieldToModal(
                  label = stringResource(R.string.endDatePickerLabel),
                  onDateSelected = { selected -> endDate = selected },
                  modifier = Modifier.fillMaxWidth().testTag(HourRecapTestTags.END_DATE))

              Spacer(Modifier.height(heightMedium))

              Button(
                  onClick = { /* later: trigger ViewModel.loadRecap() */},
                  enabled = startDate != null && endDate != null,
                  modifier = Modifier.fillMaxWidth().testTag(HourRecapTestTags.GENERATE_BUTTON)) {
                    Text(stringResource(R.string.hour_recap_generate))
                  }

              Spacer(Modifier.height(heightMedium))

              Text(
                  text = stringResource(R.string.hour_recap_results_title),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
              Spacer(Modifier.height(heightSmall))

              LazyColumn(modifier = Modifier.testTag(HourRecapTestTags.RECAP_LIST)) {
                fakeRecap.forEach { (name, time) ->
                  item {
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
                                    text = time,
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
/** Preview of HourRecapScreen */
@Preview(showBackground = true)
@Composable
fun HourRecapScreenPreview() {
  HourRecapScreen(onBackClick = {})
}
