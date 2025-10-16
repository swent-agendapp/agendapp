package com.android.sample.ui.calendar

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.formatString
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.calendar.utils.DateTimeUtils
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

object AddEventTestTags {
  const val TITLE_TEXT_FIELD = "title_text_field"
  const val DESCRIPTION_TEXT_FIELD = "description_text_field"
  const val START_DATE_FIELD = "start_date_field"
  const val END_RECURRENCE_FIELD = "end_date_field"
  const val START_TIME_FIELD = "start_time_button"
  const val END_TIME_BUTTON = "end_time_button"
  const val PERSONAL_NOTE_TEXT_FIELD = "personal_note_text_field"
  const val RECURRENCE_STATUS_DROPDOWN = "recurrence_status_dropdown"
  const val NEXT_BUTTON = "next_button"
  const val BACK_BUTTON = "back_button"
  const val CANCEL_BUTTON = "cancel_button"
  const val CREATE_BUTTON = "create_button"
  const val FINISH_BUTTON = "finish_button"

  const val ERROR_MESSAGE = "error_message"
}

@Composable
fun AddEventTitleAndDescriptionScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onNext: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  var titleTouched by remember { mutableStateOf(false) }
  var descriptionTouched by remember { mutableStateOf(false) }

  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.addEventTitle)) },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.enterTitleAndDescription),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                  }
              Column(modifier = Modifier.weight(1f)) {
                ValidatingTextField(
                    label = stringResource(R.string.eventTitle),
                    placeholder = stringResource(R.string.eventTitlePlaceholder),
                    testTag = AddEventTestTags.TITLE_TEXT_FIELD,
                    isError = addEventViewModel.titleIsBlank() && titleTouched,
                    errorMessage = stringResource(R.string.title_empty_error),
                    value = newEventUIState.title,
                    onValueChange = { addEventViewModel.setTitle(it) },
                    onFocusChange = { focusState -> if (focusState.isFocused) titleTouched = true })
                ValidatingTextField(
                    label = stringResource(R.string.eventDescription),
                    placeholder = stringResource(R.string.eventDescriptionPlaceholder),
                    testTag = AddEventTestTags.DESCRIPTION_TEXT_FIELD,
                    isError = addEventViewModel.descriptionIsBlank() && descriptionTouched,
                    errorMessage = stringResource(R.string.description_empty_error),
                    value = newEventUIState.description,
                    onValueChange = { addEventViewModel.setDescription(it) },
                    onFocusChange = { focusState ->
                      if (focusState.isFocused) descriptionTouched = true
                    },
                    singleLine = false,
                    minLines = 12)
              }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = onNext,
            onBack = onCancel,
            backButtonText = stringResource(R.string.cancel),
            nextButtonText = stringResource(R.string.next),
            canGoNext =
                !addEventViewModel.titleIsBlank() && !addEventViewModel.descriptionIsBlank(),
            backButtonTestTag = AddEventTestTags.CANCEL_BUTTON,
            nextButtonTestTag = AddEventTestTags.NEXT_BUTTON)
      })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventTimeAndRecurrenceScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  val context = LocalContext.current

  var expanded by remember { mutableStateOf(false) }
  val recurrenceOptions = RecurrenceStatus.entries.toList()
  val selectedRecurrence = newEventUIState.recurrenceMode

  val now = Calendar.getInstance()

  var showStartTimePicker by remember { mutableStateOf(false) }
  var startTimePickerHour by remember { mutableIntStateOf(now.get(Calendar.HOUR_OF_DAY)) }
  var startTimePickerMinute by remember { mutableIntStateOf(now.get(Calendar.MINUTE)) }

  var showEndTimePicker by remember { mutableStateOf(false) }
  var endTimePickerHour by remember { mutableIntStateOf(now.get(Calendar.HOUR_OF_DAY) + 1) }
  var endTimePickerMinute by remember { mutableIntStateOf(now.get(Calendar.MINUTE)) }

  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.addEventTitle)) },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.enterTimeAndRecurrence),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                  }

              Column(modifier = Modifier.weight(1f)) {
                if (!recurrenceOptions.isEmpty()) {
                  ExposedDropdownMenuBox(
                      expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedRecurrence.formatString(),
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
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors())
                        ExposedDropdownMenu(
                            expanded = expanded, onDismissRequest = { expanded = false }) {
                              recurrenceOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name) },
                                    onClick = {
                                      addEventViewModel.setRecurrenceMode(option)
                                      expanded = false
                                    })
                              }
                            }
                      }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DatePickerFieldToModal(
                    label = stringResource(R.string.datePickerLabel),
                    modifier = Modifier.testTag(AddEventTestTags.START_DATE_FIELD),
                    onDateSelected = { date -> addEventViewModel.setDate(date) })

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.startTime),
                          modifier = Modifier.weight(1f),
                          textAlign = TextAlign.Center)
                      OutlinedButton(
                          onClick = { showStartTimePicker = true },
                          modifier = Modifier.weight(1f)) {
                            Text(
                                text =
                                    DateTimeUtils.formatLocalDateTimeToTime(
                                        newEventUIState.startDate,
                                        LocalTime.of(
                                            newEventUIState.startHour,
                                            newEventUIState.startMinute)))
                          }
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.endTime),
                          modifier = Modifier.weight(1f),
                          textAlign = TextAlign.Center)
                      OutlinedButton(
                          onClick = { showEndTimePicker = true }, modifier = Modifier.weight(1f)) {
                            Text(
                                text =
                                    DateTimeUtils.formatLocalDateTimeToTime(
                                        newEventUIState.startDate,
                                        LocalTime.of(
                                            newEventUIState.endHour, newEventUIState.endMinute)))
                          }
                    }

                if (selectedRecurrence != RecurrenceStatus.OneTime) {
                  Spacer(modifier = Modifier.height(16.dp))

                  DatePickerFieldToModal(
                      label = stringResource(R.string.recurrenceEndPickerLabel),
                      modifier = Modifier.testTag(AddEventTestTags.END_RECURRENCE_FIELD),
                      onDateSelected = { date ->
                        addEventViewModel.setRecurrenceEndTime(
                            LocalDateTime.of(date, LocalTime.now())
                                .atZone(ZoneId.systemDefault())
                                .toInstant())
                      })
                }
              }

              if (showStartTimePicker) {
                TimePickerDialog(
                        context,
                        { _, hour: Int, minute: Int ->
                          addEventViewModel.setStartHour(hour)
                          addEventViewModel.setStartMinute(minute)
                        },
                        newEventUIState.startHour,
                        newEventUIState.startMinute,
                        false)
                    .show()
                showStartTimePicker = false
              }

              if (showEndTimePicker) {
                TimePickerDialog(
                        context,
                        { _, hour: Int, minute: Int ->
                          addEventViewModel.setEndHour(hour)
                          addEventViewModel.setEndMinute(minute)
                        },
                        newEventUIState.endHour,
                        newEventUIState.endMinute,
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
            canGoNext = true,
            backButtonTestTag = AddEventTestTags.BACK_BUTTON,
            nextButtonTestTag = AddEventTestTags.NEXT_BUTTON)
      })
}

@Composable
fun AddEventAttendantScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onCreate: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()

  val allParticipants =
      listOf(
          "Alice",
          "Bob",
          "Charlie",
          "David",
          "Eve",
          "Frank") // Placeholder for all possible participants

  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.addEventTitle)) },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.selectAttendants),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                  }
              Card(
                  modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
                  shape = RoundedCornerShape(12.dp),
                  elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    // Scrollable list
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                      items(allParticipants) { participant ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                      if (newEventUIState.participants.contains(participant)) {
                                        addEventViewModel.removeParticipant(participant)
                                      } else {
                                        addEventViewModel.addParticipant(participant)
                                      }
                                    }
                                    .padding(vertical = 8.dp)) {
                              Checkbox(
                                  checked = newEventUIState.participants.contains(participant),
                                  onCheckedChange = { checked ->
                                    if (checked) {
                                      addEventViewModel.addParticipant(participant)
                                    } else {
                                      addEventViewModel.removeParticipant(participant)
                                    }
                                  })
                              Spacer(modifier = Modifier.width(8.dp))
                              Text(text = participant)
                            }
                        Divider()
                      }
                    }
                  }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = {
              addEventViewModel.addEvent()
              onCreate()
            },
            onBack = onBack,
            backButtonText = stringResource(R.string.goBack),
            nextButtonText = stringResource(R.string.create),
            canGoNext = addEventViewModel.allFieldsValid(),
            backButtonTestTag = AddEventTestTags.BACK_BUTTON,
            nextButtonTestTag = AddEventTestTags.CREATE_BUTTON)
      })
}

@Composable
fun AddEventConfirmationScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onFinish: () -> Unit = {},
) {
  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.addEventTitle)) },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.confirmationMessage),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                  }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = onFinish,
            nextButtonText = stringResource(R.string.finish),
            canGoBack = false,
            canGoNext = true,
            nextButtonTestTag = AddEventTestTags.FINISH_BUTTON)
      })
}

@Composable
fun BottomNavigationButtons(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    canGoBack: Boolean = true,
    backButtonText: String = "",
    canGoNext: Boolean = false,
    nextButtonText: String = "",
    backButtonTestTag: String = "",
    nextButtonTestTag: String = ""
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        if (canGoBack) {
          OutlinedButton(
              onClick = onBack,
              Modifier.size(width = 120.dp, height = 60.dp).testTag(backButtonTestTag)) {
                Text(backButtonText)
              }
        }
        Button(
            onClick = onNext,
            Modifier.size(width = 120.dp, height = 60.dp).testTag(nextButtonTestTag),
            enabled = canGoNext) {
              Text(nextButtonText)
            }
      }
}

@Preview(showBackground = true)
@Composable
fun AddEventTitleAndDescriptionScreenPreview() {
  AddEventTitleAndDescriptionScreen()
}

@Preview(showBackground = true)
@Composable
fun AddEventTimeAndRecurrenceScreenPreview() {
  AddEventTimeAndRecurrenceScreen()
}

@Preview(showBackground = true)
@Composable
fun AddEventAttendantScreenPreview() {
  AddEventAttendantScreen()
}

@Preview(showBackground = true)
@Composable
fun AddEventConfirmationScreenPreview() {
  AddEventConfirmationScreen()
}
