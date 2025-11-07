package com.android.sample.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

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
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        if (canGoBack) {
          OutlinedButton(
              onClick = onBack,
              Modifier.size(width = 120.dp, height = 60.dp).testTag(backButtonTestTag)) {
                Text(backButtonText)
              }
        }
        Button(
            onClick = onNext,
            Modifier.size(width = 120.dp, height = 60.dp).testTag(nextButtonTestTag),
            enabled = canGoNext) {
              Text(nextButtonText)
            }
      }
}
