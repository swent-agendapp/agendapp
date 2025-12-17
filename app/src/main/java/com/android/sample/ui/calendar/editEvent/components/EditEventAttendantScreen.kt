package com.android.sample.ui.calendar.editEvent.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.components.ExtraEventToggle
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.common.MemberSelectionList
import com.android.sample.ui.common.MemberSelectionListOptions
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.PaddingHuge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.WeightExtraHeavy

// Assisted by AI
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
  val eventParticipants = uiState.participants

  // This state controls if the warning dialog is visible.
  // It starts as true so the dialog is shown when the screen is first displayed.
  var showWarningDialog by remember { mutableStateOf(true) }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.edit_event_participants_screen_title),
            canGoBack = false)
      },
      content = { paddingValues ->
        // Show the warning dialog when the screen is first displayed.
        if (showWarningDialog) {
          AlertDialog(
              onDismissRequest = {
                // When user presses back, we navigate back.
                onBack()
              },
              confirmButton = {
                TextButton(
                    modifier = Modifier.testTag(EditEventTestTags.ATTENDANCE_WARNING_ACK_BUTTON),
                    onClick = {
                      // When user presses OK, we hide the dialog.
                      showWarningDialog = false
                    }) {
                      Text(
                          text = stringResource(R.string.warning_got_it),
                          fontWeight = FontWeight.Bold)
                    }
              },
              title = { Text(stringResource(R.string.warning_title)) },
              text = { Text(stringResource(R.string.warning_participant_modification)) },
          )
        }
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = PaddingHuge, vertical = PaddingLarge)
                    .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              ExtraEventToggle(
                  isExtra = uiState.isExtraEvent,
                  onToggle = editEventViewModel::setIsExtra,
                  modifier = Modifier.fillMaxWidth().padding(bottom = PaddingLarge),
                  toggleTestTag = EditEventTestTags.EXTRA_EVENT_TOGGLE,
                  descriptionTestTag = EditEventTestTags.EXTRA_EVENT_DESCRIPTION,
              )
              MemberSelectionList(
                  modifier =
                      Modifier.weight(WeightExtraHeavy)
                          .fillMaxWidth()
                          .padding(vertical = PaddingSmall)
                          .testTag(EditEventTestTags.PARTICIPANTS_LIST),
                  members = uiState.users,
                  selectedMembers = eventParticipants,
                  onSelectionChanged = { newSelection ->
                    val oldParticipants = eventParticipants
                    val newParticipants = newSelection

                    // With the set of removed participants (was in old but disappeared in new),
                    // remove them of the Event
                    (oldParticipants - newParticipants).forEach { removedParticipant ->
                      editEventViewModel.removeParticipant(removedParticipant)
                    }

                    // With the set of added participants (is in new but not already in old),
                    // add them to the Event
                    (newParticipants - oldParticipants).forEach { addedParticipant ->
                      editEventViewModel.addParticipant(addedParticipant)
                    }
                  },
                  options =
                      MemberSelectionListOptions(
                          memberTagBuilder = { "${EditEventTestTags.PARTICIPANTS_LIST}_$it" }))
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = {
              editEventViewModel.saveEditEventChanges()
              onSave()
            },
            onBack = onBack,
            backButtonText = stringResource(R.string.common_cancel),
            nextButtonText = stringResource(R.string.common_save),
            canGoNext = !showWarningDialog,
            backButtonTestTag = EditEventTestTags.BACK_BUTTON,
            nextButtonTestTag = EditEventTestTags.SAVE_BUTTON)
      })
}

@Preview(showBackground = true, name = "Edit Event Attendees Preview")
@Composable
fun EditEventAttendantScreenPreview() {
  EditEventAttendantScreen()
}
