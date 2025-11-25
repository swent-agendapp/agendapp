package com.android.sample.ui.calendar.addEvent.components

import android.app.TimePickerDialog
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.labelRes
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightExtraHeavy
import com.android.sample.ui.theme.WeightMedium

/**
 * Second step of event creation flow: select start/end date and time, plus optional recurrence
 * rules.
 *
 * The user selects date using a modal date picker and time with native TimePicker. Validation
 * ensures start time is not after end time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventTimeAndRecurrenceScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  val context = LocalContext.current

  val timeIsCoherent by
      remember(newEventUIState) {
        derivedStateOf {
          !addEventViewModel.startTimeIsAfterEndTime() &&
              !addEventViewModel.startTimeIsAfterEndRecurrenceTime()
        }
      }

  var expanded by remember { mutableStateOf(false) }
  val recurrenceOptions = RecurrenceStatus.entries.toList()
  val selectedRecurrence = newEventUIState.recurrenceMode

  var showStartTimePicker by remember { mutableStateOf(false) }
  var showEndTimePicker by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            modifier = Modifier.testTag(MapScreenTestTags.MAP_TITLE),
            title = stringResource(R.string.addEventTitle),
            canGoBack = false)
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = PaddingExtraLarge)
                    .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(WeightMedium).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.enterTimeAndRecurrence),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(AddEventTestTags.INSTRUCTION_TEXT))
                  }

              Column(modifier = Modifier.weight(WeightExtraHeavy)) {
                if (recurrenceOptions.isNotEmpty()) {
                  ExposedDropdownMenuBox(
                    expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                      value = stringResource(selectedRecurrence.labelRes()),
                      onValueChange = {},
                      readOnly = true,
                      label = { Text(stringResource(R.string.recurrenceMenuLabel)) },
                      trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                      },
                      modifier =
                        Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, true)
                          .fillMaxWidth()
                          .testTag(AddEventTestTags.RECURRENCE_STATUS_DROPDOWN),
                      shape = RoundedCornerShape(CornerRadiusLarge),
                      colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                      expanded = expanded, onDismissRequest = { expanded = false }) {
                      recurrenceOptions.forEach { option ->
                        DropdownMenuItem(
                          text = { Text(stringResource(option.labelRes())) },
                          onClick = {
                            addEventViewModel.setRecurrenceMode(option)
                            expanded = false
                          },
                          modifier =
                            Modifier.testTag(AddEventTestTags.recurrenceTag(option))
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(SpacingLarge))

                DatePickerFieldToModal(
                  label = stringResource(R.string.startDatePickerLabel),
                  modifier = Modifier.testTag(AddEventTestTags.START_DATE_FIELD),
                  onDateSelected = { date ->
                    addEventViewModel.setStartInstant(
                      DateTimeUtils.instantWithDate(newEventUIState.startInstant, date = date)
                    )
                  },
                  initialInstant = newEventUIState.startInstant,
                  enabled = true
                )

                Spacer(modifier = Modifier.height(SpacingSmall))

                DatePickerFieldToModal(
                  label = stringResource(R.string.endDatePickerLabel),
                  modifier = Modifier.testTag(AddEventTestTags.END_DATE_FIELD),
                  onDateSelected = { date ->
                    addEventViewModel.setEndInstant(
                      DateTimeUtils.instantWithDate(newEventUIState.endInstant, date = date)
                    )
                  },
                  initialInstant = newEventUIState.endInstant,
                  enabled = true
                )

                Spacer(modifier = Modifier.height(SpacingLarge))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = stringResource(R.string.startTime),
                    modifier = Modifier.weight(WeightExtraHeavy),
                    textAlign = TextAlign.Center
                  )
                  OutlinedButton(
                    onClick = { showStartTimePicker = true },
                    modifier =
                      Modifier.weight(WeightExtraHeavy)
                        .testTag(AddEventTestTags.START_TIME_BUTTON)
                  ) {
                    Text(
                      text =
                        DateTimeUtils.formatInstantToTime(newEventUIState.startInstant)
                    )
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = stringResource(R.string.endTime),
                    modifier = Modifier.weight(WeightExtraHeavy),
                    textAlign = TextAlign.Center
                  )
                  OutlinedButton(
                    onClick = { showEndTimePicker = true },
                    modifier =
                      Modifier.weight(WeightExtraHeavy)
                        .testTag(AddEventTestTags.END_TIME_BUTTON)
                  ) {
                    Text(
                      text =
                        DateTimeUtils.formatInstantToTime(newEventUIState.endInstant)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(SpacingSmall))

                DatePickerFieldToModal(
                  label = stringResource(R.string.recurrenceEndPickerLabel),
                  modifier = Modifier.testTag(AddEventTestTags.END_RECURRENCE_FIELD),
                  onDateSelected = { date ->
                    addEventViewModel.setRecurrenceEndTime(
                      DateTimeUtils.instantWithDate(
                        newEventUIState.startInstant, date = date
                      )
                    )
                  },
                  enabled = (selectedRecurrence != RecurrenceStatus.OneTime),
                  initialInstant = newEventUIState.recurrenceEndInstant
                )
              }


              if (showStartTimePicker) {
                TimePickerDialog(
                        context,
                        { _, hour: Int, minute: Int ->
                          val newInstant =
                              DateTimeUtils.instantWithTime(
                                  instant = newEventUIState.startInstant,
                                  hour = hour,
                                  minute = minute)
                          addEventViewModel.setStartInstant(newInstant)
                        },
                        DateTimeUtils.getInstantHour(newEventUIState.startInstant),
                        DateTimeUtils.getInstantMinute(newEventUIState.startInstant),
                        false)
                    .show()
                showStartTimePicker = false
              }

              if (showEndTimePicker) {
                TimePickerDialog(
                        context,
                        { _, hour: Int, minute: Int ->
                          val newInstant =
                              DateTimeUtils.instantWithTime(
                                  instant = newEventUIState.endInstant,
                                  hour = hour,
                                  minute = minute)
                          addEventViewModel.setEndInstant(newInstant)
                        },
                        DateTimeUtils.getInstantHour(newEventUIState.endInstant),
                        DateTimeUtils.getInstantMinute(newEventUIState.endInstant),
                        false)
                    .show()
                showEndTimePicker = false
              }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = onNext,
            onBack = onBack,
            backButtonText = stringResource(R.string.goBack),
            nextButtonText = stringResource(R.string.next),
            canGoNext = timeIsCoherent,
            backButtonTestTag = AddEventTestTags.BACK_BUTTON,
            nextButtonTestTag = AddEventTestTags.NEXT_BUTTON)
      })
}

@Preview(showBackground = true)
@Composable
fun AddEventTimeAndRecurrenceScreenPreview() {
  AddEventTimeAndRecurrenceScreen()
}
