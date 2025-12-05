package com.android.sample.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.Weight

// Assisted by AI

/**
 * A composable that shows bottom navigation buttons (Back and Next). 2 versions are supported:
 * - version 1: simple buttons with default styling
 * - version 2: buttons with custom styling (red color scheme, rounded corners, sized buttons)
 */
@Composable
fun BottomNavigationButtons(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    canGoBack: Boolean = true,
    backButtonText: String = "",
    canGoNext: Boolean = false,
    nextButtonText: String = "",
    backButtonTestTag: String = "",
    nextButtonTestTag: String = "",
) {

  Row(
      modifier = Modifier.fillMaxWidth().padding(PaddingMedium).padding(bottom = PaddingLarge),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        if (canGoBack) {
          SecondaryButton(
              modifier = Modifier.testTag(backButtonTestTag).weight(Weight),
              text = backButtonText,
              onClick = onBack,
          )
        }
        PrimaryButton(
            modifier = Modifier.testTag(nextButtonTestTag).weight(Weight),
            text = nextButtonText,
            onClick = onNext,
            enabled = canGoNext)
      }
}
