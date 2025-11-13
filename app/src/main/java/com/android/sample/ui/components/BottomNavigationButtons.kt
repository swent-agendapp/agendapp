package com.android.sample.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.Weight
import com.android.sample.ui.theme.WeightLight
import com.android.sample.ui.theme.WeightMedium
import com.android.sample.ui.theme.heightLarge
import com.android.sample.ui.theme.widthLarge

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
          enabled = canGoNext
        )
      }
}
