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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.components.EventSummaryCard
import com.android.sample.ui.theme.WeightHeavy
import com.android.sample.ui.theme.WeightVeryHeavy
import com.android.sample.utils.EventColor
import java.time.Duration
import java.time.Instant

// --- Test tags to use in UI tests ---
object EventOverviewScreenTestTags {
  const val SCREEN_ROOT = "event_overview_screen_root"
  const val BACK_BUTTON = "event_overview_back"
  const val TOP_BAR = "event_overview_top_bar"
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
    eventOverviewViewModel: EventOverviewViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
  val overviewUIState by eventOverviewViewModel.uiState.collectAsState()
  var event = overviewUIState.event
  var participantNames = overviewUIState.participantsNames

  // Fetch the event and its participant display names
  LaunchedEffect(eventId) {
    try {
      // ================= before (with wrong ViewModel) =================
      // event = calendarViewModel.getEventById(eventId)

      // Later (when the Add flow will propose a list of User that are in the Auth repository
      // instead of a fake name's list) :

      //      participantNames = calendarViewModel.getParticipantNames(eventId)

      // Note : we can't use it now because the AddViewModel add user ID like "Alice", "Bob" but no
      // User with these ids exist in the Auth repo
      // => the getParticipantName doesn't find any user with an "Alice" id, and return an empty
      // list

      // To still see something coherent with what we "add", we update it like so :
      //      participantNames =
      //          event?.participants?.toList() ?: emptyList() // in reality these are the users'
      // ids !

      // ================= now =================
      eventOverviewViewModel.loadEvent(eventId)
      eventOverviewViewModel.loadParticipantNames(eventId)
    } catch (_: Exception) {
      // If the event is not found or another error occurs, surface an empty UI state
      event = null
      participantNames = emptyList()
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = modifier.testTag(EventOverviewScreenTestTags.TOP_BAR),
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
                    .testTag(EventOverviewScreenTestTags.SCREEN_ROOT),
            contentAlignment = Alignment.TopCenter) {
              // Constrain the card so it does not take the whole width on large screens.
              // EventSummaryCard internally uses fillMaxWidth(), so we wrap it in a box
              // with a max width and a fraction of the screen width.
              Box(modifier = Modifier.fillMaxWidth(WeightVeryHeavy).fillMaxHeight(WeightHeavy)) {
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
