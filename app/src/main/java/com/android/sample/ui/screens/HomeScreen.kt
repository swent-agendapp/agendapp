package com.android.sample.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.android.sample.resources.C

/** Home screen with navigation buttons for other screens. */
@Composable
fun HomeScreen(
    onNavigateToEdit: (String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.home_screen }) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Button(onClick = { onNavigateToEdit("E001") }) { Text("Go to Edit Event") }
          Spacer(modifier = Modifier.height(12.dp))
          Button(onClick = onNavigateToCalendar) { Text("Go to Calendar") }
          Spacer(modifier = Modifier.height(12.dp))
          Button(onClick = onNavigateToSettings) { Text("Go to Settings") }
        }
  }
}
