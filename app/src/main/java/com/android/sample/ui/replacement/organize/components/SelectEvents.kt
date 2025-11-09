package com.android.sample.ui.replacement.organize.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.WeightVeryHeavy
import com.android.sample.ui.theme.WeightVeryLight

@Composable
fun SelectEventScreen(onNext: () -> Unit = {}, onBack: () -> Unit = {}) {

  val selectedEvents: List<Event> = emptyList()
  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.organize_replacement)) },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = PaddingExtraLarge)
                    .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround) {
              Box(
                  modifier = Modifier.weight(WeightVeryLight).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text =
                            "Select the date range for which <substituted> needs a replacement", // to be put in strings.xml
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag(AddEventTestTags.INSTRUCTION_TEXT))
                  }
              Box(modifier = Modifier.weight(WeightVeryHeavy).fillMaxWidth())
            } // Empty content waiting for implementation
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = { onNext() },
            onBack = onBack,
            backButtonText = stringResource(R.string.goBack),
            nextButtonText = stringResource(R.string.create),
            canGoNext = selectedEvents.isNotEmpty(),
            backButtonTestTag = AddEventTestTags.BACK_BUTTON,
            nextButtonTestTag = AddEventTestTags.CREATE_BUTTON)
      })
}

@Preview
@Composable
fun SelectEventScreenPreview() {
  SelectEventScreen()
}
