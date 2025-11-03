package com.android.sample.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.sample.R

object HomeTestTags {
  const val ROOT = "home_screen"
  const val CALENDAR_BUTTON = "calendar_button"
  const val SETTINGS_BUTTON = "settings_button"
}

/** Home screen with navigation buttons for other screens. */
@Composable
fun HomeScreen(
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = HomeTestTags.ROOT }) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Button(onClick = { onNavigateToEdit("E001") }) {
            Text(stringResource(R.string.home_go_to_edit_event))
          }
          Spacer(modifier = Modifier.height(12.dp))
          Button(
              modifier = Modifier.testTag(HomeTestTags.CALENDAR_BUTTON),
              onClick = onNavigateToCalendar) {
                Text(stringResource(R.string.home_go_to_calendar))
              }
          Spacer(modifier = Modifier.height(12.dp))
          Button(
              modifier = Modifier.testTag(HomeTestTags.SETTINGS_BUTTON),
              onClick = onNavigateToSettings) {
                Text(stringResource(R.string.home_go_to_settings))
              }
        }
  }
}
