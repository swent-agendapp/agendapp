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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.WeightVeryHeavy

/**
 * Final step of event creation flow: confirmation message displayed after successful event
 * creation.
 *
 * Only shows a single "Finish" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventConfirmationScreen(
    onFinish: () -> Unit = {},
) {
  Scaffold(
      topBar = {
          TopAppBar(
              title = {
                  Text(
                      stringResource(R.string.addEventTitle),
                      modifier = Modifier.testTag(MapScreenTestTags.MAP_TITLE))
              })
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
                  modifier = Modifier.weight(WeightVeryHeavy).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.confirmationMessage),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(AddEventTestTags.INSTRUCTION_TEXT))
                  }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = onFinish,
            nextButtonText = stringResource(R.string.finish),
            canGoBack = false,
            canGoNext = true,
            nextButtonTestTag = AddEventTestTags.FINISH_BUTTON)
      })
}

@Preview(showBackground = true)
@Composable
fun AddEventConfirmationScreenPreview() {
  AddEventConfirmationScreen()
}
