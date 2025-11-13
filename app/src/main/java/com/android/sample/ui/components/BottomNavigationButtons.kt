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
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DangerRed
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
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
    version: Int = 1
) {
  if (version == 2) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(PaddingLarge),
        horizontalArrangement = Arrangement.SpaceEvenly) {
          // Cancel / Back button
          if (canGoBack) {
            OutlinedButton(
                onClick = onBack,
                modifier =
                    Modifier.size(width = widthLarge, height = heightLarge)
                        .testTag(backButtonTestTag),
                shape = RoundedCornerShape(CornerRadiusLarge),
                border = BorderStroke(BorderWidthThin, DangerRed), // Red border
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)) {
                  Text(
                      text = backButtonText,
                      modifier =
                          Modifier.padding(horizontal = PaddingMedium, vertical = PaddingSmall))
                }
          }

          // Next / Confirm button
          Button(
              onClick = onNext,
              modifier =
                  Modifier.size(width = widthLarge, height = heightLarge)
                      .testTag(nextButtonTestTag),
              shape = RoundedCornerShape(CornerRadiusLarge),
              enabled = canGoNext,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = DangerRed,
                      contentColor = Color.White,
                      disabledContainerColor = DangerRed.copy(alpha = WeightLight),
                      disabledContentColor = Color.White.copy(alpha = WeightMedium))) {
                Text(
                    text = nextButtonText,
                    modifier =
                        Modifier.padding(horizontal = PaddingMedium, vertical = PaddingSmall))
              }
        }
  } else {
    Row(
        modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
        horizontalArrangement = Arrangement.SpaceEvenly) {
          if (canGoBack) {
            // Cancel / Back button
            OutlinedButton(onClick = onBack, Modifier.testTag(backButtonTestTag)) {
              Text(
                  text = backButtonText,
                  modifier = Modifier.padding(horizontal = PaddingMedium, vertical = PaddingSmall))
            }
          }
          // Next / Confirm button
          Button(onClick = onNext, Modifier.testTag(nextButtonTestTag), enabled = canGoNext) {
            Text(
                text = nextButtonText,
                modifier = Modifier.padding(horizontal = PaddingMedium, vertical = PaddingSmall))
          }
        }
  }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationButtonsPreviewV1() {
  BottomNavigationButtons(
      canGoBack = true,
      backButtonText = "Back",
      canGoNext = true,
      nextButtonText = "Next",
  )
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationButtonsPreviewV2() {
  BottomNavigationButtons(
      canGoBack = true,
      backButtonText = "Back",
      canGoNext = true,
      nextButtonText = "Next",
      version = 2,
  )
}
