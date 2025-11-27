package com.android.sample.ui.calendar.addEvent

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.addEvent.components.AddEventAttendantBottomBar
import com.android.sample.ui.calendar.addEvent.components.AddEventAttendantScreen
import com.android.sample.ui.calendar.addEvent.components.AddEventConfirmationBottomBar
import com.android.sample.ui.calendar.addEvent.components.AddEventConfirmationScreen
import com.android.sample.ui.calendar.addEvent.components.AddEventTimeAndRecurrenceBottomBar
import com.android.sample.ui.calendar.addEvent.components.AddEventTimeAndRecurrenceScreen
import com.android.sample.ui.calendar.addEvent.components.AddEventTitleAndDescriptionBottomBar
import com.android.sample.ui.calendar.addEvent.components.AddEventTitleAndDescriptionScreen
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.map.MapScreenTestTags

// Assisted by AI

/** Contains the test tags used across the Add Event screen UI. */
object AddEventTestTags {
  const val INSTRUCTION_TEXT = "instruction_text"
  const val TITLE_TEXT_FIELD = "title_text_field"
  const val DESCRIPTION_TEXT_FIELD = "description_text_field"
  const val START_DATE_FIELD = "start_date_field"
  const val END_DATE_FIELD = "end_date_field"
  const val END_RECURRENCE_FIELD = "end_recurrence_field"
  const val START_TIME_BUTTON = "start_time_button"
  const val END_TIME_BUTTON = "end_time_button"
  const val CHECK_BOX_EMPLOYEE = "check_box_employee"
  const val RECURRENCE_STATUS_DROPDOWN = "recurrence_status_dropdown"
  const val NEXT_BUTTON = "next_button"
  const val BACK_BUTTON = "back_button"
  const val CANCEL_BUTTON = "cancel_button"
  const val CREATE_BUTTON = "create_button"
  const val FINISH_BUTTON = "finish_button"
  const val ERROR_MESSAGE = "error_message"

  fun recurrenceTag(status: RecurrenceStatus): String =
      when (status) {
        RecurrenceStatus.OneTime -> "recurrence_one_time"
        RecurrenceStatus.Daily -> "recurrence_one_time"
        RecurrenceStatus.Weekly -> "recurrence_weekly"
        RecurrenceStatus.Monthly -> "recurrence_monthly"
        RecurrenceStatus.Yearly -> "recurrence_yearly"
      }
}

/**
 * Entry-point composable for the Add Event feature.
 *
 * Displays a sequential multi-step wizard using a local `currentStep` state. Screens never navigate
 * by themselves — instead they call `onNext()` / `onBack()` and this composable decides which
 * screen to show.
 *
 * @param addEventViewModel Shared ViewModel holding the draft event data.
 * @param onFinish Callback triggered when the workflow is completed.
 * @param onCancel Callback triggered when the user cancels the flow.
 */
@Composable
fun AddEventScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onFinish: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
  val uiState by addEventViewModel.uiState.collectAsState()
  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            modifier = Modifier.testTag(MapScreenTestTags.MAP_TITLE),
            title = stringResource(R.string.addEventTitle),
            canGoBack = true,
            onClick = {
              onCancel()
              addEventViewModel.resetUiState()
            })
      },
      content = { padding ->
        when (uiState.step) {
          AddEventStep.TITLE_AND_DESC ->
              AddEventTitleAndDescriptionScreen(
                  modifier = Modifier.padding(padding),
                  addEventViewModel = addEventViewModel,
              )
          AddEventStep.TIME_AND_RECURRENCE ->
              AddEventTimeAndRecurrenceScreen(
                  modifier = Modifier.padding(padding),
                  addEventViewModel = addEventViewModel,
              )
          AddEventStep.ATTENDEES ->
              AddEventAttendantScreen(
                  modifier = Modifier.padding(padding), addEventViewModel = addEventViewModel)
          AddEventStep.CONFIRMATION ->
              AddEventConfirmationScreen(
                  modifier = Modifier.padding(padding),
              )
        }
      },
      bottomBar = {
        when (uiState.step) {
          AddEventStep.TITLE_AND_DESC ->
              AddEventTitleAndDescriptionBottomBar(
                  addEventViewModel = addEventViewModel,
                  onNext = { addEventViewModel.nextStep() },
              )
          AddEventStep.TIME_AND_RECURRENCE ->
              AddEventTimeAndRecurrenceBottomBar(
                  addEventViewModel = addEventViewModel,
                  onNext = { addEventViewModel.nextStep() },
                  onBack = { addEventViewModel.previousStep() })
          AddEventStep.ATTENDEES ->
              AddEventAttendantBottomBar(
                  addEventViewModel = addEventViewModel,
                  onCreate = {
                    addEventViewModel.addEvent()
                    addEventViewModel.nextStep()
                  },
                  onBack = { addEventViewModel.previousStep() },
              )
          AddEventStep.CONFIRMATION ->
              AddEventConfirmationBottomBar(
                  onFinish = {
                    onFinish()
                    addEventViewModel.resetUiState()
                  })
        }
      })
}
