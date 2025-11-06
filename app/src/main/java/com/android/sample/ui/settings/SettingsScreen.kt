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
import com.android.sample.ui.settings.SettingsScreenTestTags.BACK_BUTTON

object SettingsScreenTestTags {
  const val ROOT = "settings_screen"
  const val BACK_BUTTON = "back_button"
  const val PROFILE_BUTTON = "profile_button"
}
/** Settings screen with navigation to profile. */
@Composable
fun SettingsScreen(
    languageOptions: List<LanguageOption>,
    selectedLanguageTag: String,
    onLanguageSelected: (LanguageOption) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = SettingsScreenTestTags.ROOT }) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(stringResource(R.string.settings_screen_title))
          Spacer(modifier = Modifier.height(16.dp))
          Button(
              modifier = Modifier.testTag(SettingsScreenTestTags.PROFILE_BUTTON),
              onClick = onNavigateToProfile) {
                Text(stringResource(R.string.settings_profile_button))
              }
          Spacer(modifier = Modifier.height(32.dp))
          LanguageSelectionSection(
              options = languageOptions,
              selectedLanguageTag = selectedLanguageTag,
              onLanguageSelected = onLanguageSelected)
          Spacer(modifier = Modifier.height(32.dp))
          Button(modifier = Modifier.testTag(BACK_BUTTON), onClick = onNavigateBack) {
            Text(stringResource(R.string.common_back))
          }
        }
  }
}
