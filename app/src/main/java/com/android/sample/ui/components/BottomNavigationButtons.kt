package com.android.sample.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall

@Composable
fun BottomNavigationButtons(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    canGoBack: Boolean = true,
    backButtonText: String = "",
    canGoNext: Boolean = false,
    nextButtonText: String = "",
    backButtonTestTag: String = "",
    nextButtonTestTag: String = ""
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        if (canGoBack) {
          OutlinedButton(onClick = onBack, Modifier.testTag(backButtonTestTag)) {
            Text(
                text = backButtonText,
                modifier = Modifier.padding(horizontal = PaddingMedium, vertical = PaddingSmall))
          }
        }
        Button(onClick = onNext, Modifier.testTag(nextButtonTestTag), enabled = canGoNext) {
          Text(
              text = nextButtonText,
              modifier = Modifier.padding(horizontal = PaddingMedium, vertical = PaddingSmall))
        }
      }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationButtonsPreview() {
  BottomNavigationButtons(
      canGoBack = true,
      backButtonText = "Back",
      canGoNext = true,
      nextButtonText = "Next",
  )
}
