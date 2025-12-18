package com.android.sample.ui.calendar.eventOverview

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.calendar.components.EventSummaryCard
import com.android.sample.ui.common.LoadingOverlay
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.EventPalette
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import java.time.Duration
import java.time.Instant

// --- Test tags to use in UI tests ---
object EventOverviewScreenTestTags {
  const val SCREEN_ROOT = "event_overview_screen_root"
  const val BACK_BUTTON = "event_overview_back"
  const val TOP_BAR = "event_overview_top_bar"
  const val DELETE_BUTTON = "delete_event_button"
  const val MODIFY_BUTTON = "modify_event_button"
  const val ASK_TO_BE_REPLACED_BUTTON = "ask_to_be_replaced_button"
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

  // Fetch the event
  LaunchedEffect(eventId) { eventOverviewViewModel.loadEvent(eventId) }

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
        SecondaryPageTopBar(
            modifier = modifier.testTag(EventOverviewScreenTestTags.TOP_BAR),
            title = stringResource(R.string.event_overview_title),
            onClick = onBackClick,
            actions = {
              IconButton(
                  onClick = { showDeleteDialog.value = true },
                  modifier = Modifier.testTag(EventOverviewScreenTestTags.DELETE_BUTTON)) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error)
                  }
            },
            backButtonTestTags = EventOverviewScreenTestTags.BACK_BUTTON)
      },
      bottomBar = {
        if (event != null) {
          Column(modifier = Modifier.fillMaxWidth().padding(PaddingMedium)) {
            // Later : condition to display the Modify button
            // if ( user is admin ) { SecondaryButton( ... ) }
            SecondaryButton(
                modifier = Modifier.testTag(EventOverviewScreenTestTags.MODIFY_BUTTON),
                text = stringResource(R.string.modify),
                onClick = { onEditClick(event.id) })

            // Later : condition to display the Replacement button
            // if (participantNames.contains( current user name )) { PrimaryButton( ... ) }
            PrimaryButton(
                modifier = Modifier.testTag(EventOverviewScreenTestTags.ASK_TO_BE_REPLACED_BUTTON),
                text = stringResource(R.string.replacement_ask_to_be_replaced),
                onClick = { /* later : create a replacement */})
          }
        }
      }) { innerPadding ->
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag(EventOverviewScreenTestTags.SCREEN_ROOT),
            contentAlignment = Alignment.TopCenter) {
              // Keep horizontal (and bottom) padding around the card so it does not stretch
              // edge‑to‑edge.
              Box(modifier = Modifier.padding(PaddingExtraLarge)) {
                event?.let { EventSummaryCard(event = it, participantNames = participantNames) }
              }
            }
      }
  if (overviewUIState.isLoading) {
    LoadingOverlay()
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
          organizationId = "org1",
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
          category =
              EventCategory(
                  organizationId = "org1", label = "Category A", color = EventPalette.Blue),
          location = null)

  EventOverviewScreen(eventId = event.id, onBackClick = {})
}
