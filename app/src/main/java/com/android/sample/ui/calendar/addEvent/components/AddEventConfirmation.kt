package com.android.sample.ui.calendar.addEvent.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.components.EventSummaryCard
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.theme.PaddingExtraLarge

/**
 * Final step of event creation flow: confirmation message displayed after successful event
 * creation.
 *
 * Only shows a single "Finish" button.
 */
@Composable
fun AddEventConfirmationScreen(
    modifier: Modifier = Modifier,
    addEventViewModel: AddEventViewModel = viewModel(),
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  val draftEvent = newEventUIState.draftEvent

  // Fetch the draft Event
  LaunchedEffect(Unit) { addEventViewModel.loadDraftEvent() }

  EventSummaryCard(
      modifier = modifier.padding(PaddingExtraLarge),
      event = draftEvent,
      participantNames = newEventUIState.users.filter { it.id in draftEvent.participants })
}

@Composable
fun AddEventConfirmationBottomBar(
    onCreate: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  BottomNavigationButtons(
      onNext = onCreate,
      onBack = onBack,
      nextButtonText = stringResource(R.string.create),
      backButtonText = stringResource(R.string.goBack),
      canGoNext = true,
      nextButtonTestTag = AddEventTestTags.CREATE_BUTTON,
      backButtonTestTag = AddEventTestTags.BACK_BUTTON)
}

@Preview(showBackground = true)
@Composable
fun AddEventConfirmationScreenPreview() {
  AddEventConfirmationScreen()
}
