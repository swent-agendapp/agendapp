package com.android.sample.ui.calendar.addEvent.components

import StepHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.common.MemberSelectionList
import com.android.sample.ui.common.MemberSelectionListOptions
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DefaultCardElevation
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.WeightExtraHeavy

/**
 * Third step of event creation flow: allows selecting participants for the event.
 *
 * User selects the participants from a scrollable list with checkboxes.
 *
 * On "Create", the ViewModel persists the event.
 */
@Composable
fun AddEventAttendantScreen(
    modifier: Modifier = Modifier,
    addEventViewModel: AddEventViewModel = viewModel(),
) {

  val newEventUIState by addEventViewModel.uiState.collectAsState()

  Column(
      modifier = modifier.fillMaxSize().padding(horizontal = PaddingExtraLarge),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        Spacer(modifier = Modifier.height(SpacingExtraLarge))
        StepHeader(
            stepText = stringResource(R.string.add_event_step_3_of_3),
            title = stringResource(R.string.add_event_attendees_title),
            subtitle = stringResource(R.string.add_event_attendees_subtitle),
            icon = { Icon(Icons.Outlined.Group, contentDescription = null) },
            progress = 1f)

        Spacer(modifier = Modifier.padding(vertical = PaddingSmall))

        Card(
            modifier =
                Modifier.fillMaxWidth().weight(WeightExtraHeavy).padding(vertical = PaddingSmall),
            shape = RoundedCornerShape(CornerRadiusLarge),
            elevation = CardDefaults.cardElevation(DefaultCardElevation)) {
              MemberSelectionList(
                  members = newEventUIState.users,
                  selectedMembers = newEventUIState.participants.toSet(),
                  onSelectionChanged = { selection ->
                    (newEventUIState.participants.toSet() - selection).forEach { removed ->
                      addEventViewModel.removeParticipant(removed)
                    }

                    (selection - newEventUIState.participants.toSet()).forEach { added ->
                      addEventViewModel.addParticipant(added)
                    }
                  },
                  modifier = Modifier.fillMaxSize(),
                  options =
                      MemberSelectionListOptions(
                          isSingleSelection = false,
                          listTestTag = AddEventTestTags.LIST_USER,
                      ),
              )
            }
      }
}

@Composable
fun AddEventAttendantBottomBar(
    addEventViewModel: AddEventViewModel = viewModel(),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()

  val canGoNext by
      remember(newEventUIState) { derivedStateOf { newEventUIState.participants.isNotEmpty() } }

  BottomNavigationButtons(
      onNext = onNext,
      onBack = onBack,
      backButtonText = stringResource(R.string.goBack),
      nextButtonText = stringResource(R.string.next),
      canGoNext = canGoNext,
      backButtonTestTag = AddEventTestTags.BACK_BUTTON,
      nextButtonTestTag = AddEventTestTags.NEXT_BUTTON)
}

@Preview(showBackground = true)
@Composable
fun AddEventAttendantScreenPreview() {
  AddEventAttendantScreen()
}
