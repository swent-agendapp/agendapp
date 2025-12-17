package com.android.sample.ui.calendar.editEvent.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.calendar.components.eventSummaryComponents.ParticipantsSection
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.category.components.CategorySelector
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.BorderWidthThick
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.WeightExtraHeavy

// Assisted by AI

// Minimum number of lines for the description input field.
private const val DESCRIPTION_MIN_LINES = 4
/**
 * **EditEventScreen**
 *
 * A composable screen that allows users to **edit** the details of an existing calendar event. The
 * screen includes fields for title, description, start/end dates and times, recurrence, and
 * participants. It is fully synchronized with [EditEventViewModel].
 *
 * ### Parameters:
 *
 * @param eventId The unique identifier of the event to be edited.
 * @param editEventViewModel The [EditEventViewModel] managing event state.
 * @param onSave Called when the user saves the changes.
 * @param onCancel Called when the user cancels the editing.
 * @param onEditParticipants Called when the user wants to edit participants.
 * @param skipLoad For testing purposes to skip loading the event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    editEventViewModel: EditEventViewModel = viewModel(),
    onNavigateToEditCategories: () -> Unit = {},
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
  // refresh categories when the screen is re-displayed (after editing categories)
  val lifecycleOwner = LocalLifecycleOwner.current

  LaunchedEffect(lifecycleOwner) {
    lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      editEventViewModel.loadCategories()
    }
  }
  // Participants names
  val names =
      if (uiState.participants.isNotEmpty()) {
        uiState.participants.toList()
      } else {
        emptyList()
      }

  var showStartTimePicker by remember { mutableStateOf(false) }
  var showEndTimePicker by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(title = stringResource(R.string.edit_event_title), canGoBack = false)
      },
      content = { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
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

              // Color
              item {
                CategorySelector(
                    selectedCategory = uiState.category,
                    onCategorySelected = { editEventViewModel.setCategory(it) },
                    onNavigateToEditCategories = onNavigateToEditCategories,
                    testTag = EditEventTestTags.CATEGORY_SELECTOR,
                    categories = uiState.categoriesList,
                    isLoading = uiState.isLoadingCategories)
                Spacer(modifier = Modifier.height(SpacingLarge))
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
                Spacer(modifier = Modifier.height(SpacingExtraLarge))
              }

              // Start & End Dates
              item {
                key(
                    uiState
                        .startInstant) { // Use the latest value from uiState as the initial value
                      DatePickerFieldToModal(
                          label = stringResource(R.string.edit_event_start_date_label),
                          modifier = Modifier.testTag(EditEventTestTags.START_DATE_FIELD),
                          onDateSelected = { date ->
                            editEventViewModel.setStartInstant(
                                DateTimeUtils.instantWithDate(uiState.startInstant, date))
                          },
                          enabled = true,
                          initialInstant = uiState.startInstant)
                    }
                Spacer(modifier = Modifier.height(SpacingLarge))
              }

              item {
                key(uiState.endInstant) {
                  DatePickerFieldToModal(
                      label = stringResource(R.string.edit_event_end_date_label),
                      modifier = Modifier.testTag(EditEventTestTags.END_DATE_FIELD),
                      onDateSelected = { date ->
                        editEventViewModel.setEndInstant(
                            DateTimeUtils.instantWithDate(uiState.endInstant, date))
                      },
                      enabled = true,
                      initialInstant = uiState.endInstant)
                }
                Spacer(modifier = Modifier.height(SpacingLarge))
              }

              // Start & End Time Pickers
              item {
                Column(modifier = Modifier.fillMaxWidth()) {

                  // Start time row
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.edit_event_start_time_label),
                            modifier = Modifier.weight(WeightExtraHeavy),
                            textAlign = TextAlign.Center)
                        OutlinedButton(
                            onClick = { showStartTimePicker = true },
                            modifier =
                                Modifier.weight(WeightExtraHeavy)
                                    .testTag(EditEventTestTags.START_TIME_BUTTON)) {
                              Text(DateTimeUtils.formatInstantToTime(uiState.startInstant))
                            }
                      }

                  Spacer(modifier = Modifier.height(SpacingMedium))

                  // End time row
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.edit_event_end_time_label),
                            modifier = Modifier.weight(WeightExtraHeavy),
                            textAlign = TextAlign.Center)
                        OutlinedButton(
                            onClick = { showEndTimePicker = true },
                            modifier =
                                Modifier.weight(WeightExtraHeavy)
                                    .testTag(EditEventTestTags.END_TIME_BUTTON)) {
                              Text(DateTimeUtils.formatInstantToTime(uiState.endInstant))
                            }
                      }
                }

                Spacer(modifier = Modifier.height(SpacingExtraLarge))
              }

              // Notifications (implement later if needed)
              /**
               * item { Spacer(modifier = Modifier.height(SpacingMedium))
               * NotificationSection(editEventViewModel = editEventViewModel) }
               */

              // Participants
              item {
                key(
                    uiState
                        .participants) { // Use the latest value from uiState as the initial value
                      Card(shape = RoundedCornerShape(CornerRadiusLarge)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
                            horizontalAlignment = Alignment.Start) {
                              ParticipantsSection(participantNames = names, showHeader = false)
                              Spacer(modifier = Modifier.height(SpacingLarge))
                              SecondaryButton(
                                  modifier =
                                      Modifier.testTag(EditEventTestTags.EDIT_PARTICIPANTS_BUTTON),
                                  onClick = onEditParticipants,
                                  text =
                                      stringResource(R.string.edit_event_edit_participants_button))
                            }
                      }
                    }
              }
            }
      },
      bottomBar = {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = BorderWidthThick,
            color = MaterialTheme.colorScheme.outlineVariant)
        BottomNavigationButtons(
            onBack = onCancel,
            backButtonText = stringResource(R.string.common_cancel),
            canGoBack = true,
            backButtonTestTag = EditEventTestTags.CANCEL_BUTTON,
            onNext = {
              editEventViewModel.saveEditEventChanges()
              onSave()
            },
            nextButtonText = stringResource(R.string.common_save),
            canGoNext = editEventViewModel.allFieldsValid(),
            nextButtonTestTag = EditEventTestTags.SAVE_BUTTON)
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

@Preview(showBackground = true, name = "Edit Event Screen Preview")
@Composable
fun EditEventScreenPreview() {
  EditEventScreen(eventId = "PREVIEW123", skipLoad = true)
}
