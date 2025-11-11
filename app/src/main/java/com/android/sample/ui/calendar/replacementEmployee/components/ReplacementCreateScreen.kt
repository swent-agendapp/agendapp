package com.android.sample.ui.calendar.replacementEmployee.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.theme.*

// Assisted by AI

object ReplacementEmployeeCreateTestTags {
  const val SELECT_EVENT_BUTTON = "replacement_select_event_button"
  const val CHOOSE_DATE_RANGE_BUTTON = "replacement_choose_date_range_button"
  const val BACK_BUTTON = "replacement_create_back_button"
}

/**
 * Screen for employees to request a replacement. Contains two main actions: selecting an event or
 * choosing a date range.
 */
@Composable
fun ReplacementCreateScreen(
    onSelectEvent: () -> Unit = {},
    onChooseDateRange: () -> Unit = {},
    onBack: () -> Unit = {}
) {
  Scaffold(
      topBar = {
        TopTitleBar(
            title = stringResource(R.string.replacement_create_title),
            canNavigateBack = true,
            onBack = onBack)
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = PaddingExtraLarge),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              // "Select a event" button
              OutlinedButton(
                  onClick = onSelectEvent,
                  modifier =
                      Modifier.fillMaxWidth(WeightMedium)
                          .padding(vertical = PaddingMedium)
                          .testTag(ReplacementEmployeeCreateTestTags.SELECT_EVENT_BUTTON),
                  shape = MaterialTheme.shapes.extraLarge) {
                    Text(
                        text = stringResource(R.string.replacement_create_select_event),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary)
                  }

              // "Choose a date range" button
              OutlinedButton(
                  onClick = onChooseDateRange,
                  modifier =
                      Modifier.fillMaxWidth(WeightHeavy)
                          .padding(vertical = PaddingMedium)
                          .testTag(ReplacementEmployeeCreateTestTags.CHOOSE_DATE_RANGE_BUTTON),
                  shape = MaterialTheme.shapes.extraLarge) {
                    Text(
                        text = stringResource(R.string.replacement_create_choose_date_range),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary)
                  }
            }
      })
}

@Preview(showBackground = true, name = "Replacement Create Screen")
@Composable
fun ReplacementCreateScreenPreview() {
  ReplacementCreateScreen()
}
