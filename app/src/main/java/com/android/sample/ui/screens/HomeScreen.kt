package com.android.sample.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.android.sample.R
import com.android.sample.ui.theme.SpacingMedium

object HomeTestTags {
  const val ROOT = "home_screen"
  const val CALENDAR_BUTTON = "calendar_button"
  const val SETTINGS_BUTTON = "settings_button"
  const val MAP_BUTTON = "map_button"
  const val REPLACEMENT_BUTTON = "replacement_button"
}

/** Home screen with navigation buttons for other screens. */
@Composable
fun HomeScreen(
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToReplacement: () -> Unit = {}
) {
  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = HomeTestTags.ROOT }) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Button(onClick = { onNavigateToEdit("E001") }) {
            Text(stringResource(R.string.home_go_to_edit_event))
          }
          Spacer(modifier = Modifier.height(SpacingMedium))
          Button(
              modifier = Modifier.testTag(HomeTestTags.CALENDAR_BUTTON),
              onClick = onNavigateToCalendar) {
                Text(stringResource(R.string.home_go_to_calendar))
              }
          Spacer(modifier = Modifier.height(SpacingMedium))
          Button(
              modifier = Modifier.testTag(HomeTestTags.SETTINGS_BUTTON),
              onClick = onNavigateToSettings) {
                Text(stringResource(R.string.home_go_to_settings))
              }
          Spacer(modifier = Modifier.height(SpacingMedium))
          Button(modifier = Modifier.testTag(HomeTestTags.MAP_BUTTON), onClick = onNavigateToMap) {
            Text("Go to Map")
          }
          Spacer(modifier = Modifier.height(SpacingMedium))
          Button(
              modifier = Modifier.testTag(HomeTestTags.REPLACEMENT_BUTTON),
              onClick = onNavigateToReplacement) {
                Text("Go to Replacement")
              }
        }
  }
}
