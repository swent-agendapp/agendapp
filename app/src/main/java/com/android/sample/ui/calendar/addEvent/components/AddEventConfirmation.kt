package com.android.sample.ui.calendar.addEvent.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.components.EventSummaryCard
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.theme.PaddingExtraLarge

/**
 * Final step of event creation flow: confirmation message displayed after successful event
 * creation.
 *
 * Only shows a single "Finish" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventConfirmationScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onCreate: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            modifier = Modifier.testTag(MapScreenTestTags.MAP_TITLE),
            title = stringResource(R.string.addEventTitle),
            canGoBack = false)
      },
      content = { paddingValues ->
        val currentEvent = addEventViewModel.getDraftEvent()
        EventSummaryCard(
            modifier = Modifier.padding(paddingValues).padding(PaddingExtraLarge),
            event = currentEvent,
            participantNames = currentEvent.participants.toList())
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = onCreate,
            onBack = onBack,
            nextButtonText = stringResource(R.string.create),
            backButtonText = stringResource(R.string.modify),
            canGoNext = true,
            nextButtonTestTag = AddEventTestTags.CREATE_BUTTON,
            backButtonTestTag = AddEventTestTags.BACK_BUTTON)
      })
}

@Preview(showBackground = true)
@Composable
fun AddEventConfirmationScreenPreview() {
  AddEventConfirmationScreen()
}
