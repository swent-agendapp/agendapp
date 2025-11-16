package com.android.sample.ui.calendar.addEvent.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventTestTags.CHECK_BOX_EMPLOYEE
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightExtraHeavy

/**
 * Third step of event creation flow: allows selecting participants for the event.
 *
 * User selects the participants from a scrollable list with checkboxes.
 *
 * On "Create", the ViewModel persists the event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventAttendantScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onCreate: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  val allFieldsValid by
      remember(newEventUIState) { derivedStateOf { addEventViewModel.allFieldsValid() } }

  val allParticipants =
      listOf(
          "Anaïs",
          "Ania",
          "Benjamin",
          "Célestin",
          "Chiara",
          "Coralie",
          "David",
          "Diana",
          "Emi",
          "Emilien",
          "Giada",
          "Héloïse",
          "Jael",
          "Laura",
          "Ludovic",
          "Maé",
          "Mara",
          "Maud",
          "Maxime",
          "Maya",
          "Miki",
          "Roby",
          "Solene",
          "Yukié",
          "Zoé") // Placeholder for all possible participants
  // note : these temporary names are inspired from the real Stakeholder's employee's names

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            modifier = Modifier.testTag(MapScreenTestTags.MAP_TITLE),
            title = stringResource(R.string.addEventTitle),
            canGoBack = false)
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = PaddingExtraLarge)
                    .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(WeightExtraHeavy).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.selectAttendants),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(AddEventTestTags.INSTRUCTION_TEXT))
                  }
              Card(
                  modifier =
                      Modifier.weight(WeightExtraHeavy)
                          .fillMaxWidth()
                          .padding(vertical = PaddingSmall),
                  shape = RoundedCornerShape(CornerRadiusLarge),
                  elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    // Scrollable list
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(PaddingMedium)) {
                      items(allParticipants) { participant ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                      val action =
                                          if (participant in newEventUIState.participants)
                                              addEventViewModel::removeParticipant
                                          else addEventViewModel::addParticipant
                                      action(participant)
                                    }
                                    .padding(vertical = PaddingSmall)
                                    .testTag(CHECK_BOX_EMPLOYEE)) {
                              Checkbox(
                                  checked = newEventUIState.participants.contains(participant),
                                  onCheckedChange = { checked ->
                                    val action =
                                        if (checked) addEventViewModel::addParticipant
                                        else addEventViewModel::removeParticipant
                                    action(participant)
                                  })
                              Spacer(modifier = Modifier.width(SpacingSmall))
                              Text(text = participant)
                            }
                        HorizontalDivider(
                            Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                      }
                    }
                  }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = { onCreate() },
            onBack = onBack,
            backButtonText = stringResource(R.string.goBack),
            nextButtonText = stringResource(R.string.create),
            canGoNext = allFieldsValid,
            backButtonTestTag = AddEventTestTags.BACK_BUTTON,
            nextButtonTestTag = AddEventTestTags.CREATE_BUTTON)
      })
}

@Preview(showBackground = true)
@Composable
fun AddEventAttendantScreenPreview() {
  AddEventAttendantScreen()
}
