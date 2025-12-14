package com.android.sample.ui.calendar.addEvent.components

import StepHeader
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.calendar.components.ValidatingTextField
import com.android.sample.ui.category.components.CategorySelector
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.GeneralPaletteDark
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall

private const val DESCRIPTION_FIELD_MIN_LINES = 12

/** First step of the event creation flow. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventTitleAndDescriptionScreen(
    modifier: Modifier = Modifier,
    addEventViewModel: AddEventViewModel = viewModel(),
) {
    val secondary = if (isSystemInDarkTheme()) GeneralPaletteDark.Secondary else GeneralPalette.Secondary
    val newEventUIState by addEventViewModel.uiState.collectAsState()

  var titleTouched by remember { mutableStateOf(false) }

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = PaddingExtraLarge)
              .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        Spacer(modifier = Modifier.height(SpacingExtraLarge))
        StepHeader(
            stepText = stringResource(R.string.add_event_step_1_of_2),
            title = stringResource(R.string.add_event_create_title),
            subtitle = "",
            icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
            progress = 0.3f)

        Spacer(modifier = Modifier.height(SpacingExtraLarge))

        ValidatingTextField(
            label = stringResource(R.string.eventTitle),
            placeholder = stringResource(R.string.eventTitlePlaceholder),
            testTag = AddEventTestTags.TITLE_TEXT_FIELD,
            isError = addEventViewModel.titleIsBlank() && titleTouched,
            errorMessage = stringResource(R.string.title_empty_error),
            value = newEventUIState.title,
            onValueChange = { addEventViewModel.setTitle(it) },
            onFocusChange = { focusState -> if (focusState.isFocused) titleTouched = true })

        Spacer(modifier = Modifier.height(SpacingLarge))

        CategorySelector(
            selectedCategory = newEventUIState.category,
            onCategorySelected = { addEventViewModel.setCategory(it) },
            testTag = AddEventTestTags.CATEGORY_SELECTOR,
        )

        Spacer(modifier = Modifier.height(SpacingLarge))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
          Text(
              text = stringResource(R.string.eventDescription),
              style = MaterialTheme.typography.labelLarge)
          Spacer(modifier = Modifier.padding(horizontal = SpacingSmall))
          Text(
              text = stringResource(R.string.optional_label),
              style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(SpacingSmall))

        ValidatingTextField(
            label = "",
            placeholder = stringResource(R.string.eventDescriptionPlaceholder),
            testTag = AddEventTestTags.DESCRIPTION_TEXT_FIELD,
            isError = false,
            errorMessage = "",
            value = newEventUIState.description,
            onValueChange = { addEventViewModel.setDescription(it) },
            onFocusChange = {},
            singleLine = false,
            minLines = DESCRIPTION_FIELD_MIN_LINES)

        Spacer(modifier = Modifier.height(SpacingExtraLarge))
      }
}

@Composable
fun AddEventTitleAndDescriptionBottomBar(
    addEventViewModel: AddEventViewModel = viewModel(),
    onNext: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
  val newEventUIState by addEventViewModel.uiState.collectAsState()
  val titleValid by
      remember(newEventUIState) { derivedStateOf { !addEventViewModel.titleIsBlank() } }

  BottomNavigationButtons(
      onNext = onNext,
      onBack = onCancel,
      backButtonText = stringResource(R.string.cancel),
      nextButtonText = stringResource(R.string.next),
      canGoNext = titleValid,
      backButtonTestTag = AddEventTestTags.CANCEL_BUTTON,
      nextButtonTestTag = AddEventTestTags.NEXT_BUTTON)
}

@Preview(showBackground = true)
@Composable
fun AddEventTitleAndDescriptionScreenPreview() {
  AddEventTitleAndDescriptionScreen()
}
