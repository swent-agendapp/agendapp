package com.android.sample.ui.replacement.organize.components

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
import com.android.sample.R
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.components.DatePickerFieldToModal
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.CornerRadiusMedium
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.WeightVeryHeavy
import java.time.Instant

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
) {

  // Later handled by the viewmodel
  var startInstant by remember { mutableStateOf(Instant.now()) }
  var endInstant by remember { mutableStateOf(Instant.now().plusSeconds(3600)) }
  val isRangeInvalid = endInstant.isBefore(startInstant)
  var substitutedUser = "example user" // to be provided by the viewmodel

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
                  modifier = Modifier.weight(WeightVeryHeavy).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text =
                            stringResource(R.string.select_replacement_date_range, substitutedUser),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(AddEventTestTags.INSTRUCTION_TEXT))
                  }

              Column(modifier = Modifier.weight(WeightVeryHeavy)) {
                Spacer(modifier = Modifier.height(SpacingLarge))

                DatePickerFieldToModal(
                    label = stringResource(R.string.startDatePickerLabel),
                    modifier = Modifier.testTag(AddEventTestTags.START_DATE_FIELD),
                    onDateSelected = { date ->
                      startInstant = DateTimeUtils.instantWithDate(startInstant, date = date)
                    },
                    initialInstant = startInstant)

                Spacer(modifier = Modifier.height(SpacingExtraLarge))

                DatePickerFieldToModal(
                    label = stringResource(R.string.endDatePickerLabel),
                    modifier = Modifier.testTag(AddEventTestTags.END_DATE_FIELD),
                    onDateSelected = { date ->
                      endInstant = DateTimeUtils.instantWithDate(endInstant, date = date)
                    },
                    initialInstant = endInstant)
              }
              AnimatedVisibility(visible = isRangeInvalid) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(top = SpacingLarge)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(CornerRadiusMedium))
                            .padding(vertical = PaddingMedium, horizontal = PaddingMedium),
                    contentAlignment = Alignment.Center) {
                      Text(
                          text = stringResource(R.string.invalidDateRangeMessage),
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
            canGoNext = !isRangeInvalid,
            backButtonTestTag = AddEventTestTags.BACK_BUTTON,
            nextButtonTestTag = AddEventTestTags.NEXT_BUTTON)
      })
}

@Preview(showBackground = true)
@Composable
fun SelectDateRangeScreenPreview() {
  SelectDateRangeScreen()
}
