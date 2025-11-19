package com.android.sample.ui.calendar.eventOverview

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.components.EventSummaryCard
import com.android.sample.ui.theme.EventPalette
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.WeightHeavy
import com.android.sample.ui.theme.WeightVeryHeavy
import java.time.Duration
import java.time.Instant

// --- Test tags to use in UI tests ---
object EventOverviewScreenTestTags {
  const val SCREEN_ROOT = "event_overview_screen_root"
  const val BACK_BUTTON = "event_overview_back"
  const val TOP_BAR = "event_overview_top_bar"
  const val DELETE_BUTTON = "delete_event_button"
  const val MODIFY_BUTTON = "modify_event_button"
  const val DIALOG_DELETE_BUTTON = "dialog_delete_button"
  const val DIALOG_CANCEL_BUTTON = "dialog_cancel_button"
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
    onEditClick: (String) -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
  val overviewUIState by eventOverviewViewModel.uiState.collectAsState()
  val event = overviewUIState.event
  val participantNames = overviewUIState.participantsNames
  val errorMsg = overviewUIState.errorMsg
  val showDeleteDialog = remember { mutableStateOf(false) }
  val isDeleteSuccessful = overviewUIState.isDeleteSuccessful

  // Navigate back when deletion is successful
  LaunchedEffect(isDeleteSuccessful) {
    if (isDeleteSuccessful) {
      onDeleteClick()
    }
  }

  // Fetch the event and its participant display names
  LaunchedEffect(eventId) {
    eventOverviewViewModel.loadEvent(eventId)
    eventOverviewViewModel.loadParticipantNames()
  }

  val context = LocalContext.current

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      eventOverviewViewModel.clearErrorMsg()
    }
  }

  // Delete confirmation dialog
  if (showDeleteDialog.value && event != null) {
    DeleteEventConfirmationDialog(
        eventTitle = event.title,
        onConfirm = {
          showDeleteDialog.value = false
          eventOverviewViewModel.deleteEvent(eventId)
        },
        onDismiss = { showDeleteDialog.value = false })
  }

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = modifier.testTag(EventOverviewScreenTestTags.TOP_BAR),
            title = {
              Text(
                  stringResource(R.string.event_overview_title),
                  style = MaterialTheme.typography.titleLarge)
            },
            navigationIcon = {
              IconButton(
                  onClick = onBackClick,
                  modifier = modifier.testTag(EventOverviewScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.goBack))
                  }
            })
      },
      bottomBar = {
        if (event != null) {
          BottomNavigationButtons(
              onBack = { showDeleteDialog.value = true },
              backButtonText = stringResource(R.string.delete),
              canGoBack = true,
              backButtonTestTag = EventOverviewScreenTestTags.DELETE_BUTTON,
              onNext = { onEditClick(event.id) },
              nextButtonText = stringResource(R.string.modify),
              canGoNext = true,
              nextButtonTestTag = EventOverviewScreenTestTags.MODIFY_BUTTON,
          )
        }
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

/** DeleteEventConfirmationDialog */
@Composable
fun DeleteEventConfirmationDialog(
    eventTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = onConfirm,
            modifier = Modifier.testTag(EventOverviewScreenTestTags.DIALOG_DELETE_BUTTON)) {
              Text(
                  text = stringResource(R.string.delete),
                  color = MaterialTheme.colorScheme.error,
                  fontWeight = FontWeight.Bold)
            }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(EventOverviewScreenTestTags.DIALOG_CANCEL_BUTTON)) {
              Text(stringResource(R.string.cancel))
            }
      },
      title = { Text("${stringResource(R.string.delete)} $eventTitle") },
      text = { Text(stringResource(R.string.delete_event_message)) })
}

// -------------------------------- Preview --------------------------------

@Preview(showBackground = true)
@Composable
private fun EventOverviewScreenPreview() {
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
          color = EventPalette.Green)

  EventOverviewScreen(eventId = event.id, onBackClick = {})
}
