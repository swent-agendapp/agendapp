package com.android.sample.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp

object HomeTestTags {
  const val ROOT = "home_screen"
  const val ADD_EVENT_BUTTON = "add_event_button"
  const val SETTINGS_BUTTON = "settings_button"
  const val MAP_BUTTON = "map_button"
}

/** Home screen with navigation buttons for other screens. */
@Composable
fun HomeScreen(
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
) {
  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = HomeTestTags.ROOT }) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Button(onClick = { onNavigateToEdit("E001") }) { Text("Go to Edit Event") }
          Spacer(modifier = Modifier.height(12.dp))
          Button(
              modifier = Modifier.testTag(HomeTestTags.ADD_EVENT_BUTTON),
              onClick = onNavigateToCalendar) {
                Text("Go to Calendar")
              }
          Spacer(modifier = Modifier.height(12.dp))
          Button(
              modifier = Modifier.testTag(HomeTestTags.SETTINGS_BUTTON),
              onClick = onNavigateToSettings) {
                Text("Go to Settings")
              }
          Spacer(modifier = Modifier.height(12.dp))
          Button(modifier = Modifier.testTag(HomeTestTags.MAP_BUTTON), onClick = onNavigateToMap) {
            Text("Go to Map")
          }
        }
  }
}
