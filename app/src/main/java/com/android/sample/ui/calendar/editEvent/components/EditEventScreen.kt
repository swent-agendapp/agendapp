package com.android.sample.ui.calendar.editEvent.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.labelRes
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.heightLarge
import com.android.sample.ui.theme.widthLarge

// Assisted by AI
private const val DESCRIPTION_MIN_LINES = 4
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
            modifier =
                Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally) {
              item {
                Text(
                    text = stringResource(R.string.edit_event_instruction),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.padding(vertical = PaddingMedium).testTag("edit_instruction_text"))
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
                    minLines = DESCRIPTION_MIN_LINES)
              }

              // Start & End Dates
              item {
                Spacer(modifier = Modifier.height(SpacingMedium))
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
                Spacer(modifier = Modifier.height(SpacingMedium))
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
                Spacer(modifier = Modifier.height(SpacingMedium))
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
                Spacer(modifier = Modifier.height(SpacingMedium))
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
                          shape = RoundedCornerShape(CornerRadiusLarge),
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

              // Notifications (implement later if needed)
              /**
               * item { Spacer(modifier = Modifier.height(SpacingMedium))
               * NotificationSection(editEventViewModel = editEventViewModel) }
               */

              // Participants
              item {
                Spacer(modifier = Modifier.height(SpacingMedium))
                Text(
                    text = stringResource(R.string.edit_event_participants_label),
                    style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = onEditParticipants,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = PaddingSmall)
                            .testTag(EditEventTestTags.EDIT_PARTICIPANTS_BUTTON)) {
                      Text(stringResource(R.string.edit_event_edit_participants_button))
                    }
              }
            }
      },
      bottomBar = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(PaddingLarge),
            horizontalArrangement = Arrangement.SpaceEvenly) {
              OutlinedButton(
                  onClick = onCancel,
                  modifier =
                      Modifier.size(width = widthLarge, height = heightLarge)
                          .testTag(EditEventTestTags.CANCEL_BUTTON)) {
                    Text(stringResource(R.string.common_cancel))
                  }
              Button(
                  onClick = {
                    if (editEventViewModel.allFieldsValid()) {
                      editEventViewModel.saveEditEventChanges()
                      onSave()
                    }
                  },
                  modifier =
                      Modifier.size(width = widthLarge, height = heightLarge)
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
