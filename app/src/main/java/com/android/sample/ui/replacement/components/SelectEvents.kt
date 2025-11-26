package com.android.sample.ui.replacement.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.CalendarEventSelector
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.replacement.organize.ReplacementOrganizeTestTags
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.WeightExtraHeavy
import com.android.sample.ui.theme.WeightVeryLight
import com.android.sample.ui.theme.heightLarge

/**
 * Screen allowing the admin to **select the events** for which a member needs a replacement.
 *
 * This screen is one of the two options of the second step of the Organize Replacement workflow. It
 * displays:
 * - An instruction text describing what the user should do
 * - a calendar where events will be displayed and selectable
 * - Navigation buttons (Back / Create)
 *
 * ### UX behavior
 * - Pressing the **Next** button triggers `onNext()`
 * - Pressing the **Back** button triggers `onBack()`
 * - The **Next** button remains disabled while no event is selected
 *
 * The parent `ReplacementOrganizeScreen` owns the navigation state and workflow. This composable
 * does **not** handle navigation or persistence; it only exposes callbacks.
 *
 * @param onNext Called when the user confirms event selection and proceeds to the next step.
 * @param onBack Called when the user navigates back to the previous screen.
 * @param instruction Instruction text to display to the user.
 */
@Composable
fun SelectEventScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    title: String,
    instruction: String,
    canGoNext: Boolean = true,
    onEventClick: (Event) -> Unit = {},
    onProcessNow: (() -> Unit)? = null,
    onProcessLater: (() -> Unit)? = null,
) {
    var showProcessOptions by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = title,
            onClick = onBack,
            backButtonTestTags = ReplacementOrganizeTestTags.BACK_BUTTON,
        )
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
                  modifier = Modifier.weight(WeightVeryLight).fillMaxWidth(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = instruction,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT))
                  }
              Box(modifier = Modifier.weight(WeightExtraHeavy).fillMaxWidth()) {
                CalendarEventSelector(onEventClick = onEventClick)
              }
            }
      },
      bottomBar = {
          Column(
              modifier =
                  Modifier
                      .fillMaxWidth()
                      .padding(horizontal = PaddingExtraLarge, vertical = PaddingMedium),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(PaddingMedium),
          ) {
              BottomNavigationButtons(
                  onNext = {
                      if (onProcessNow == null && onProcessLater == null) {
                          onNext()
                      } else {
                          showProcessOptions = !showProcessOptions
                      }
                  },
                  onBack = onBack,
                  backButtonText = stringResource(R.string.goBack),
                  nextButtonText = stringResource(R.string.next),
                  canGoBack = false,
                  canGoNext = canGoNext,
                  backButtonTestTag = ReplacementOrganizeTestTags.BACK_BUTTON,
                  nextButtonTestTag = ReplacementOrganizeTestTags.NEXT_BUTTON,
              )

              AnimatedVisibility(
                  visible = showProcessOptions && onProcessNow != null && onProcessLater != null,
              ) {
                  Column(
                      modifier = Modifier.fillMaxWidth(),
                      verticalArrangement = Arrangement.spacedBy(PaddingMedium),
                  ) {
                      SecondaryButton(
                          modifier =
                              Modifier
                                  .fillMaxWidth()
                                  .testTag(ReplacementOrganizeTestTags.PROCESS_NOW_BUTTON),
                          text = stringResource(R.string.process_now),
                          onClick = { onProcessNow?.invoke() },
                      )

                      SecondaryButton(
                          modifier =
                              Modifier
                                  .fillMaxWidth()
                                  .testTag(ReplacementOrganizeTestTags.PROCESS_LATER_BUTTON),
                          text = stringResource(R.string.process_later),
                          onClick = { onProcessLater?.invoke() },
                      )
                  }
              }
          }
      })
}
