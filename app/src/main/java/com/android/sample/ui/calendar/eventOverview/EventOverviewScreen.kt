package com.android.sample.ui.calendar.eventOverview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.CalendarViewModel
import com.android.sample.ui.calendar.components.EventSummaryCard
import com.android.sample.utils.EventColor
import java.time.Duration
import java.time.Instant

// --- Test tags to use in UI tests ---
object EventOverviewScreenTestTags {
  const val SCREEN = "event_overview_screen"
  const val BACK_BUTTON = "event_overview_back"
}

/**
 * EventOverviewScreen
 *
 * A screen that shows a centered [EventSummaryCard] with a small top bar. An [onBackClick] is
 * provided for the navigation icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventOverviewScreen(
    modifier: Modifier = Modifier,
    eventId: String,
    calendarViewModel: CalendarViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
  var event by remember { mutableStateOf<Event?>(null) }
  var participantNames by remember { mutableStateOf<List<String>>(emptyList()) }

  // Fetch the event and its participant display names
  LaunchedEffect(eventId) {
    try {
      event = calendarViewModel.getEventById(eventId)
      participantNames = calendarViewModel.getParticipantNames(eventId)
    } catch (_: Exception) {
      // If the event is not found or another error occurs, surface an empty UI state
      event = null
      participantNames = emptyList()
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Event Overview", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
              IconButton(
                  onClick = onBackClick,
                  modifier = modifier.testTag(EventOverviewScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { innerPadding ->
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag(EventOverviewScreenTestTags.SCREEN),
            contentAlignment = Alignment.TopCenter) {
              // Constrain the card so it does not take the whole width on large screens.
              // EventSummaryCard internally uses fillMaxWidth(), so we wrap it in a box
              // with a max width and a fraction of the screen width.
              Box(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.73f)) {
                event?.let { EventSummaryCard(event = it, participantNames = participantNames) }
              }
            }
      }
}

// -------------------------------- Preview --------------------------------

@Preview(showBackground = true)
@Composable
private fun EventOverviewScreen_Preview() {
  val base = Instant.now()
  val event =
      Event(
          id = "p0",
          title = "Weekly stand‑up",
          description = "Short sync about progress and blockers.",
          startDate = base.plusSeconds(Duration.ofHours(9).seconds),
          endDate = base.plusSeconds(Duration.ofHours(10).seconds),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u1", "u2"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.Weekly,
          hasBeenDeleted = false,
          color = EventColor.Green)

  EventOverviewScreen(eventId = event.id, onBackClick = {})
}
