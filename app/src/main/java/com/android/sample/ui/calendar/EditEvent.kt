package com.android.sample.ui.calendar

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.labelRes
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.calendar.utils.DateTimeUtils

object EditEventTestTags {
  const val TITLE_FIELD = "edit_title_field"
  const val DESCRIPTION_FIELD = "edit_description_field"
  const val START_DATE_FIELD = "edit_start_date"
  const val END_DATE_FIELD = "edit_end_date"
  const val START_TIME_BUTTON = "edit_start_time_button"
  const val END_TIME_BUTTON = "edit_end_time_button"
  const val RECURRENCE_DROPDOWN = "edit_recurrence_dropdown"
  const val PARTICIPANTS_LIST = "edit_participants_list"
  const val SAVE_BUTTON = "edit_save_button"
  const val CANCEL_BUTTON = "edit_cancel_button"
  const val EDIT_PARTICIPANTS_BUTTON = "edit_participants_button"
  const val BACK_BUTTON = "edit_back_button"
}
// Spacing data class for consistent spacing values
// Will be useful
// data class Spacing(val small: Dp = 8.dp, val medium: Dp = 16.dp, val large: Dp = 24.dp)

/**
 * Simple one-page Edit Event screen. This view uses placeholder state until EditEventViewModel is
 * connected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    editEventViewModel: EditEventViewModel = viewModel(),
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    onEditParticipants: () -> Unit = {},
    skipLoad: Boolean = false // For testing purposes to skip loading the event
) {
  val context = LocalContext.current
  val uiState by editEventViewModel.uiState.collectAsState()

  // Load the event when the screen is first displayed
  LaunchedEffect(eventId) {
    if (!skipLoad) {
      editEventViewModel.loadEvent(eventId)
    }
  }

  var expanded by remember { mutableStateOf(false) }
  var showStartTimePicker by remember { mutableStateOf(false) }
  var showEndTimePicker by remember { mutableStateOf(false) }

  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.edit_event_title)) },
      content = { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              item {
                Text(
                    text = stringResource(R.string.edit_event_instruction),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp).testTag("edit_instruction_text"))
              }

              // Title
              item {
                ValidatingTextField(
                    label = stringResource(R.string.edit_event_title_label),
                    placeholder = stringResource(R.string.edit_event_title_placeholder),
                    testTag = EditEventTestTags.TITLE_FIELD,
                    value = uiState.title,
                    onValueChange = { editEventViewModel.setTitle(it) },
                    isError = uiState.title.isBlank(),
                    errorMessage = stringResource(R.string.edit_event_title_error))
              }

              // Description
              item {
                ValidatingTextField(
                    label = stringResource(R.string.edit_event_description_label),
                    placeholder = stringResource(R.string.edit_event_description_placeholder),
                    testTag = EditEventTestTags.DESCRIPTION_FIELD,
                    value = uiState.description,
                    onValueChange = { editEventViewModel.setDescription(it) },
                    isError = uiState.description.isBlank(),
                    errorMessage = stringResource(R.string.edit_event_description_error),
                    singleLine = false,
                    minLines = 4)
              }

              // Start & End Dates
              item {
                Spacer(modifier = Modifier.height(12.dp))
                DatePickerFieldToModal(
                    label = stringResource(R.string.edit_event_start_date_label),
                    modifier = Modifier.testTag(EditEventTestTags.START_DATE_FIELD),
                    onDateSelected = { date ->
                      editEventViewModel.setStartInstant(
                          DateTimeUtils.instantWithDate(uiState.startInstant, date))
                    },
                    initialInstant = uiState.startInstant)
              }

              item {
                DatePickerFieldToModal(
                    label = stringResource(R.string.edit_event_end_date_label),
                    modifier = Modifier.testTag(EditEventTestTags.END_DATE_FIELD),
                    onDateSelected = { date ->
                      editEventViewModel.setEndInstant(
                          DateTimeUtils.instantWithDate(uiState.endInstant, date))
                    },
                    initialInstant = uiState.endInstant)
              }

              // Start Time Picker
              item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.edit_event_start_time_label),
                          style = MaterialTheme.typography.titleMedium)
                      Button(
                          onClick = { showStartTimePicker = true },
                          modifier = Modifier.testTag(EditEventTestTags.START_TIME_BUTTON)) {
                            Text(DateTimeUtils.formatInstantToTime(uiState.startInstant))
                          }
                    }
              }

              // End Time Picker
              item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.edit_event_end_time_label),
                          style = MaterialTheme.typography.titleMedium)
                      Button(
                          onClick = { showEndTimePicker = true },
                          modifier = Modifier.testTag(EditEventTestTags.END_TIME_BUTTON)) {
                            Text(DateTimeUtils.formatInstantToTime(uiState.endInstant))
                          }
                    }
              }

              // Recurrence dropdown
              item {
                Spacer(modifier = Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                      OutlinedTextField(
                          value = stringResource(uiState.recurrenceMode.labelRes()),
                          onValueChange = {},
                          readOnly = true,
                          label = { Text(stringResource(R.string.edit_event_recurrence_label)) },
                          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                          modifier =
                              Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                  .fillMaxWidth()
                                  .testTag(EditEventTestTags.RECURRENCE_DROPDOWN),
                          shape = RoundedCornerShape(12.dp),
                          colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors())
                      ExposedDropdownMenu(
                          expanded = expanded, onDismissRequest = { expanded = false }) {
                            RecurrenceStatus.entries.forEach { option ->
                              DropdownMenuItem(
                                  text = { Text(option.name) },
                                  onClick = {
                                    editEventViewModel.setRecurrenceMode(option)
                                    expanded = false
                                  })
                            }
                          }
                    }
              }

              // Notifications
              /**
               * item { Spacer(modifier = Modifier.height(12.dp))
               * NotificationSection(editEventViewModel = editEventViewModel) }
               */

              // Participants
              item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.edit_event_participants_label),
                    style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = onEditParticipants,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag(EditEventTestTags.EDIT_PARTICIPANTS_BUTTON)) {
                      Text(stringResource(R.string.edit_event_edit_participants_button))
                    }
              }
            }
      },
      bottomBar = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly) {
              OutlinedButton(
                  onClick = onCancel,
                  modifier =
                      Modifier.size(width = 120.dp, height = 60.dp)
                          .testTag(EditEventTestTags.CANCEL_BUTTON)) {
                    Text(stringResource(R.string.common_cancel))
                  }
              Button(
                  onClick = {
                    if (editEventViewModel.allFieldsValid()) {
                      editEventViewModel.saveChanges()
                      onSave()
                    }
                  },
                  modifier =
                      Modifier.size(width = 120.dp, height = 60.dp)
                          .testTag(EditEventTestTags.SAVE_BUTTON),
                  enabled = editEventViewModel.allFieldsValid()) {
                    Text(stringResource(R.string.common_save))
                  }
            }
      })

  // Time pickers
  if (showStartTimePicker) {
    TimePickerDialog(
            context,
            { _, hour, minute ->
              editEventViewModel.setStartInstant(
                  DateTimeUtils.instantWithTime(uiState.startInstant, hour, minute))
            },
            DateTimeUtils.getInstantHour(uiState.startInstant),
            DateTimeUtils.getInstantMinute(uiState.startInstant),
            false)
        .show()
    showStartTimePicker = false
  }

  if (showEndTimePicker) {
    TimePickerDialog(
            context,
            { _, hour, minute ->
              editEventViewModel.setEndInstant(
                  DateTimeUtils.instantWithTime(uiState.endInstant, hour, minute))
            },
            DateTimeUtils.getInstantHour(uiState.endInstant),
            DateTimeUtils.getInstantMinute(uiState.endInstant),
            false)
        .show()
    showEndTimePicker = false
  }
}

/**
 * **EditEventAttendantScreen**
 *
 * A composable screen that allows users to **view**, **select**, and **update** the list of
 * participants for a calendar event. The participant list is fully synchronized with
 * [EditEventViewModel].
 *
 * ### Parameters:
 *
 * @param editEventViewModel The [EditEventViewModel] managing participant state.
 * @param onSave Called when the user confirms the changes.
 * @param onBack Called when the user cancels and navigates back.
 */
@Composable
fun EditEventAttendantScreen(
    editEventViewModel: EditEventViewModel = viewModel(),
    onSave: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  val uiState by editEventViewModel.uiState.collectAsState()
  // Placeholder for all possible participants
  // This would come from a repository or service
  val allParticipants = listOf("Alice", "Bob", "Charlie", "David", "Eve", "Frank")

  Scaffold(
      topBar = {
        TopTitleBar(title = stringResource(R.string.edit_event_participants_screen_title))
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.edit_event_select_participants_text),
                        style = MaterialTheme.typography.headlineMedium)
                  }

              Card(
                  modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
                  shape = RoundedCornerShape(12.dp),
                  elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                      items(allParticipants) { name ->
                        val isSelected = uiState.participants.contains(name)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                      if (isSelected) editEventViewModel.removeParticipant(name)
                                      else editEventViewModel.addParticipant(name)
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("${EditEventTestTags.PARTICIPANTS_LIST}_$name")) {
                              Checkbox(
                                  checked = isSelected,
                                  onCheckedChange = { checked ->
                                    if (checked) editEventViewModel.addParticipant(name)
                                    else editEventViewModel.removeParticipant(name)
                                  })
                              Spacer(modifier = Modifier.width(8.dp))
                              Text(name)
                            }
                      }
                    }
                  }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = {
              editEventViewModel.saveChanges()
              onSave()
            },
            onBack = onBack,
            backButtonText = stringResource(R.string.common_cancel),
            nextButtonText = stringResource(R.string.common_save),
            canGoNext = true,
            backButtonTestTag = EditEventTestTags.BACK_BUTTON,
            nextButtonTestTag = EditEventTestTags.SAVE_BUTTON)
      })
}
