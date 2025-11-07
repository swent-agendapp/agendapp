package com.android.sample.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.R

object LanguageSelectionScreenTestTags {
  const val ROOT = "language_selection_screen"
  const val APPLY_BUTTON = "language_apply_button"
  const val OPTION_PREFIX = "language_option_"
}

/** Screen that allows the user to pick the preferred application language. */
@Composable
fun LanguageSelectionScreen(onNavigateBack: () -> Unit = {}) {
  var selectedLanguage by rememberSaveable { mutableStateOf("en") }
  val languageOptions = listOf(
      "en" to stringResource(R.string.language_option_english),
      "fr" to stringResource(R.string.language_option_french),
      "es" to stringResource(R.string.language_option_spanish),
      "de" to stringResource(R.string.language_option_german))

  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = LanguageSelectionScreenTestTags.ROOT }) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
          Text(
              text = stringResource(R.string.language_screen_title),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold)
          Text(
              text = stringResource(R.string.language_screen_description),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            languageOptions.forEach { (code, label) ->
              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("${LanguageSelectionScreenTestTags.OPTION_PREFIX}$code"),
                  verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedLanguage == code, onClick = { selectedLanguage = code })
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                  }
            }
          }
          Spacer(modifier = Modifier.weight(1f))
          Button(
              modifier =
                  Modifier.fillMaxWidth().testTag(LanguageSelectionScreenTestTags.APPLY_BUTTON),
              onClick = onNavigateBack) {
                Text(text = stringResource(R.string.language_apply_button_label))
              }
        }
  }
}
