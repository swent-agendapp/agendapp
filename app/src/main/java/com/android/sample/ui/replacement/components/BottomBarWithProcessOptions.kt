package com.android.sample.ui.replacement.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.replacement.organize.ReplacementOrganizeTestTags
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingMedium

@Composable
fun ReplacementBottomBarWithProcessOptions(
    canGoNext: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onProcessNow: (() -> Unit)?,
    onProcessLater: (() -> Unit)?,
) {
  var showProcessOptions by remember { mutableStateOf(false) }

  Column(
      modifier =
          Modifier.fillMaxWidth().padding(horizontal = PaddingExtraLarge, vertical = PaddingMedium),
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
                Modifier.fillMaxWidth().testTag(ReplacementOrganizeTestTags.PROCESS_NOW_BUTTON),
            text = stringResource(R.string.process_now),
            onClick = { onProcessNow?.invoke() },
        )

        SecondaryButton(
            modifier =
                Modifier.fillMaxWidth().testTag(ReplacementOrganizeTestTags.PROCESS_LATER_BUTTON),
            text = stringResource(R.string.process_later),
            onClick = { onProcessLater?.invoke() },
        )
      }
    }
  }
}
