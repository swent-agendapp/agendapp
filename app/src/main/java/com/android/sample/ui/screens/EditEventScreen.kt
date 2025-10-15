package com.android.sample.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp

object EditEventTestTags {
  const val ROOT = "edit_event_screen"
  const val BACK_BUTTON = "back_button"
}

/** Edit event page. */
@Composable
fun EditEventScreen(eventId: String, onNavigateBack: () -> Unit) {
  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = EditEventTestTags.ROOT }) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Editing event ID: $eventId")
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = onNavigateBack) { Text("Back") }
        }
  }
}
