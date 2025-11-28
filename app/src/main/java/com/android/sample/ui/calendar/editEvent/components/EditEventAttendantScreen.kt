package com.android.sample.ui.calendar.editEvent.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.ElevationLow
import com.android.sample.ui.theme.PaddingHuge
import com.android.sample.ui.theme.PaddingMedium
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
  // Placeholder for all possible participants
  // This would come from a repository or service
  val allParticipants = listOf("Alice", "Bob", "Charlie", "David", "Eve", "Frank")

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.edit_event_participants_screen_title),
            canGoBack = false
        )
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(horizontal = PaddingHuge).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(WeightExtraHeavy).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.edit_event_select_participants_text),
                        style = MaterialTheme.typography.headlineMedium)
                  }

              Card(
                  modifier =
                      Modifier.weight(WeightExtraHeavy)
                          .fillMaxWidth()
                          .padding(vertical = PaddingSmall),
                  shape = RoundedCornerShape(CornerRadiusLarge),
                  elevation = CardDefaults.cardElevation(defaultElevation = ElevationLow)) {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(PaddingMedium)) {
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
                                    .padding(vertical = PaddingSmall)
                                    .testTag("${EditEventTestTags.PARTICIPANTS_LIST}_$name")) {
                              Checkbox(
                                  checked = isSelected,
                                  onCheckedChange = { checked ->
                                    if (checked) editEventViewModel.addParticipant(name)
                                    else editEventViewModel.removeParticipant(name)
                                  })
                              Spacer(modifier = Modifier.width(PaddingSmall))
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
              editEventViewModel.saveEditEventChanges()
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

@Preview(showBackground = true, name = "Edit Event Attendees Preview")
@Composable
fun EditEventAttendantScreenPreview() {
  EditEventAttendantScreen()
}
