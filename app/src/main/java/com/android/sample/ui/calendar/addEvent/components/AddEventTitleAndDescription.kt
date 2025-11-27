package com.android.sample.ui.calendar.addEvent.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.WeightExtraHeavy
import com.android.sample.ui.theme.WeightMedium

private const val DESCRIPTION_FIELD_MIN_LINES = 12

/**
 * First step of the event creation flow.
 *
 * Lets the user input the event title and description. Validation: both fields must be non-blank to
 * enable the Next button.
 *
 * @param onNext Triggered when validation passes and the user moves to step 2.
 * @param onCancel Triggered when the user cancels the flow entirely.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventTitleAndDescriptionScreen(
    addEventViewModel: AddEventViewModel = viewModel(),
    onNext: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  val titleAndDescValid by
      remember(newEventUIState) {
        derivedStateOf {
          !(addEventViewModel.titleIsBlank() || addEventViewModel.descriptionIsBlank())
        }
      }

  var titleTouched by remember { mutableStateOf(false) }
  var descriptionTouched by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            modifier = Modifier.testTag(MapScreenTestTags.MAP_TITLE),
            title = stringResource(R.string.addEventTitle),
            canGoBack = true,
            onClick = onCancel)
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
                  modifier = Modifier.weight(WeightMedium).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.enterTitleAndDescription),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(AddEventTestTags.INSTRUCTION_TEXT))
                  }
              Column(modifier = Modifier.weight(WeightExtraHeavy)) {
                ValidatingTextField(
                    label = stringResource(R.string.eventTitle),
                    placeholder = stringResource(R.string.eventTitlePlaceholder),
                    testTag = AddEventTestTags.TITLE_TEXT_FIELD,
                    isError = addEventViewModel.titleIsBlank() && titleTouched,
                    errorMessage = stringResource(R.string.title_empty_error),
                    value = newEventUIState.title,
                    onValueChange = { addEventViewModel.setTitle(it) },
                    onFocusChange = { focusState -> if (focusState.isFocused) titleTouched = true })

                ValidatingTextField(
                    label = stringResource(R.string.eventDescription),
                    placeholder = stringResource(R.string.eventDescriptionPlaceholder),
                    testTag = AddEventTestTags.DESCRIPTION_TEXT_FIELD,
                    isError = addEventViewModel.descriptionIsBlank() && descriptionTouched,
                    errorMessage = stringResource(R.string.description_empty_error),
                    value = newEventUIState.description,
                    onValueChange = { addEventViewModel.setDescription(it) },
                    onFocusChange = { focusState ->
                      if (focusState.isFocused) descriptionTouched = true
                    },
                    singleLine = false,
                    minLines = DESCRIPTION_FIELD_MIN_LINES)
              }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = onNext,
            onBack = onCancel,
            backButtonText = stringResource(R.string.cancel),
            nextButtonText = stringResource(R.string.next),
            canGoNext = titleAndDescValid,
            backButtonTestTag = AddEventTestTags.CANCEL_BUTTON,
            nextButtonTestTag = AddEventTestTags.NEXT_BUTTON)
      })
}

@Preview(showBackground = true)
@Composable
fun AddEventTitleAndDescriptionScreenPreview() {
  AddEventTitleAndDescriptionScreen()
}
