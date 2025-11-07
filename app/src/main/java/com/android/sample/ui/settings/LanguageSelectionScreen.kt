package com.android.sample.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.android.sample.R
import com.android.sample.localization.LanguageOption

object LanguageSelectionScreenTestTags {
  const val ROOT = "language_selection_screen"
  const val SAVE_BUTTON = "language_save_button"
  const val BACK_BUTTON = "language_back_button"
}

/** Screen allowing the user to select and save the preferred language. */
@Composable
fun LanguageSelectionScreen(
    languageOptions: List<LanguageOption>,
    selectedLanguageTag: String,
    onLanguageSelected: (LanguageOption) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
) {
  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = LanguageSelectionScreenTestTags.ROOT }) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          LanguageSelectionSection(
              modifier = Modifier,
              options = languageOptions,
              selectedLanguageTag = selectedLanguageTag,
              onLanguageSelected = onLanguageSelected)
          Spacer(modifier = Modifier.height(24.dp))
          Button(
              modifier = Modifier.testTag(LanguageSelectionScreenTestTags.SAVE_BUTTON),
              onClick = onSave) {
                Text(text = stringResource(id = R.string.common_save))
              }
          Spacer(modifier = Modifier.height(16.dp))
          Button(
              modifier = Modifier.testTag(LanguageSelectionScreenTestTags.BACK_BUTTON),
              onClick = onNavigateBack) {
                Text(text = stringResource(id = R.string.common_back))
              }
        }
  }
}
