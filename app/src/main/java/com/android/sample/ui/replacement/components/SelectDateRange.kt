package com.android.sample.ui.replacement.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.replacement.organize.ReplacementOrganizeTestTags
import com.android.sample.ui.theme.CornerRadiusMedium
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.WeightExtraHeavy
import java.time.Instant
import java.time.LocalDate

// Assisted by AI

/**
 * Screen allowing the admin to **select a date range** during which a member requires a
 * replacement.
 *
 * This screen is one of the two options of the second step in the *Organize Replacement* workflow.
 * Instead of selecting specific events, the admin chooses a time interval for which the member
 * needs to be replaced.
 *
 * The screen displays:
 * - An instruction text describing what the user should do
 * - Two date pickers (start date and end date), each opening a modal date picker when pressed
 * - Navigation buttons (Back / Next)
 *
 * ### UX behavior
 * - The **Next** button triggers `onNext()`
 * - The **Back** button triggers `onBack()`
 * - The **Next** button remains disabled until a valid date range is selected (`endInstant` must be
 *   after `startInstant`)
 *
 * This composable only exposes the selected date range via callback behavior. It does **not**
 * handle navigation nor state persistence — this responsibility belongs to the parent
 * `ReplacementOrganizeScreen`.
 *
 * @param onNext Called when the user confirms the date range and proceeds to the next step.
 * @param onBack Called when the user navigates back to the previous screen.
 */
@Composable
fun SelectDateRangeScreen(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    title: String = "",
    instruction: String = "",
    initialStartInstant: Instant = Instant.now(),
    initialEndInstant: Instant = Instant.now(),
    onStartDateSelected: (LocalDate) -> Unit = {},
    onEndDateSelected: (LocalDate) -> Unit = {},
    errorMessage: String = "",
    canGoNext: Boolean = false,
) {

  Scaffold(
      topBar = { TopTitleBar(title = title) },
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
                        text = instruction,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT))
                  }

              Column(modifier = Modifier.weight(WeightExtraHeavy)) {
                Spacer(modifier = Modifier.height(SpacingLarge))

                DatePickerFieldToModal(
                    label = stringResource(R.string.startDatePickerLabel),
                    modifier = Modifier.testTag(ReplacementOrganizeTestTags.START_DATE_FIELD),
                    onDateSelected = { date -> onStartDateSelected(date) },
                    enabled = true,
                    initialInstant = initialStartInstant)

                Spacer(modifier = Modifier.height(SpacingExtraLarge))

                DatePickerFieldToModal(
                    label = stringResource(R.string.endDatePickerLabel),
                    modifier = Modifier.testTag(ReplacementOrganizeTestTags.END_DATE_FIELD),
                    onDateSelected = { date -> onEndDateSelected(date) },
                    enabled = true,
                    initialInstant = initialEndInstant)
              }
              AnimatedVisibility(visible = !canGoNext) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(top = SpacingLarge)
                            .background(
                                color = Color.LightGray,
                                shape = RoundedCornerShape(CornerRadiusMedium))
                            .padding(vertical = PaddingMedium, horizontal = PaddingMedium)
                            .testTag(ReplacementOrganizeTestTags.DATE_RANGE_INVALID_TEXT),
                    contentAlignment = Alignment.Center) {
                      Text(
                          text = errorMessage,
                          style = MaterialTheme.typography.bodyMedium,
                          textAlign = TextAlign.Center)
                    }
              }
            }
      },
      bottomBar = {
        BottomNavigationButtons(
            onNext = onNext,
            onBack = onBack,
            backButtonText = stringResource(R.string.goBack),
            nextButtonText = stringResource(R.string.next),
            canGoNext = canGoNext,
            backButtonTestTag = ReplacementOrganizeTestTags.BACK_BUTTON,
            nextButtonTestTag = ReplacementOrganizeTestTags.NEXT_BUTTON)
      })
}

@Preview(showBackground = true)
@Composable
fun SelectDateRangeScreenPreview() {
  SelectDateRangeScreen(title = "Example Title", instruction = "Example Instruction")
}
