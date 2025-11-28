package com.android.sample.ui.calendar.editEvent

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.ui.calendar.editEvent.components.EditEventAttendantScreen
import com.android.sample.ui.calendar.editEvent.components.EditEventScreen

// Assisted by AI
object EditEventTestTags {
  const val ROOT = "edit_event_screen"
  const val TITLE_FIELD = "edit_title_field"
  const val COLOR_SELECTOR = "color_selector"
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

/**
 * Entry point for the Edit Event flow.
 *
 * This composable decides whether to show the main event editor screen or the participants editor
 * screen.
 */
@Composable
fun EditEventFlow(
    eventId: String,
    editEventViewModel: EditEventViewModel = viewModel(),
    onFinish: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
  val uiState by editEventViewModel.uiState.collectAsState()

  when (uiState.step) {
    EditEventStep.MAIN -> {
      EditEventScreen(
          eventId = eventId,
          editEventViewModel = editEventViewModel,
          onSave = {
            onFinish()
            editEventViewModel.resetUiState()
          },
          onCancel = {
            onCancel()
            editEventViewModel.resetUiState()
          },
          onEditParticipants = { editEventViewModel.goToAttendeesStep() })
    }
    EditEventStep.ATTENDEES -> {
      EditEventAttendantScreen(
          editEventViewModel = editEventViewModel,
          onSave = { editEventViewModel.goBackToMainStep() },
          onBack = { editEventViewModel.goBackToMainStep() })
    }
  }

  // Handle the Android system back button
  BackHandler(enabled = uiState.step == EditEventStep.ATTENDEES) {
    editEventViewModel.goBackToMainStep()
  }
}
