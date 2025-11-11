package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.components.eventSummaryComponents.DrawEventSummaryCard
import com.android.sample.ui.calendar.style.EventSummaryCardDefaults
import com.android.sample.ui.calendar.utils.buildDatePresentation
import com.android.sample.ui.calendar.utils.recurrenceLabel
import com.android.sample.utils.EventColor
import java.time.*
import java.util.Locale

/** Test tags for UI testing */
object EventSummaryCardTags {
    const val TitleText = "EventCard_Title"
    const val ToggleTitle = "EventCard_ToggleTitle"
    const val DateLine1 = "EventCard_DateLine1"
    const val DateLine2 = "EventCard_DateLine2"
    const val Multi_FromLabel = "EventCard_Multi_FromLabel"
    const val Multi_ToLabel = "EventCard_Multi_ToLabel"
    const val Multi_StartDate = "EventCard_Multi_StartDate"
    const val Multi_EndDate = "EventCard_Multi_EndDate"
    const val Multi_StartTime = "EventCard_Multi_StartTime"
    const val Multi_EndTime = "EventCard_Multi_EndTime"
    const val Recurrence = "EventCard_Recurrence"
    const val DescriptionText = "EventCard_Description"
    const val ToggleDescription = "EventCard_ToggleDescription"
    const val ParticipantsList = "EventCard_ParticipantsList"
    const val SideBar = "EventCard_SideBar"
}

/**
 * EventSummaryCard (public API)
 *
 * Renders a summary card for an [Event].
 * - This composable is responsible for deriving all values needed for the UI (dates, flags,
 *   labels).
 * - It then delegates pure drawing to [DrawEventSummaryCard], which composes small, focused
 *   sections.
 */
@Composable
fun EventSummaryCard(
    event: Event,
    modifier: Modifier = Modifier,
    participantNames: List<String> = emptyList(),
) {
  // --- Default style and text configuration ---
  val style = EventSummaryCardDefaults.style
  val textConfig = EventSummaryCardDefaults.texts
  val zone = ZoneId.systemDefault()
  val loc = Locale.getDefault()

  // --- UI state (expand/collapse) ---
  var isTitleExpanded by remember { mutableStateOf(false) }
  var didTitleOverflow by remember { mutableStateOf(false) }
  var isDescriptionExpanded by remember { mutableStateOf(false) }
  var didDescriptionOverflow by remember { mutableStateOf(false) }

  // --- Derived temporal content for rendering (single source of truth for date strings) ---
  val dateModel = remember(event, zone, loc) { buildDatePresentation(event, zone, loc) }

  // --- Colors / shapes / participants ---
  val shape = RoundedCornerShape(style.cornerRadiusDp)
  val sideColor = event.color.toComposeColor()

  // Recurrence text (hidden if no recurrence, meaning if the recurrence is OneTime)
  val recurrenceText: String? =
      if (event.recurrenceStatus != RecurrenceStatus.OneTime)
          recurrenceLabel(event.recurrenceStatus, dateModel.startZdt, loc)
      else null

  DrawEventSummaryCard(
      modifier = modifier,
      style = style,
      textConfig = textConfig,
      sideColor = sideColor,
      shape = shape,
      // Title
      titleText = event.title,
      isTitleExpanded = isTitleExpanded,
      onTitleToggle = { isTitleExpanded = !isTitleExpanded },
      onTitleOverflowChange = { didTitleOverflow = it },
      showTitleToggle = didTitleOverflow || isTitleExpanded,
      // Dates
      datePresentation = dateModel,
      // Recurrence
      recurrenceText = recurrenceText,
      // Description
      descriptionText = event.description,
      isDescriptionExpanded = isDescriptionExpanded,
      onDescriptionToggle = { isDescriptionExpanded = !isDescriptionExpanded },
      onDescriptionOverflowChange = { didDescriptionOverflow = it },
      showDescriptionToggle = didDescriptionOverflow || isDescriptionExpanded,
      // Participants
      participantNames = participantNames,
  )
}

// -------------------------------- Preview --------------------------------

private val previewEvents: List<Event> by lazy {
  val base = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
  val day = Duration.ofDays(1)
  val hour = Duration.ofHours(1)
  listOf(
      // e0) Simple event
      Event(
          id = "e0",
          title = "A simple Event",
          description = "A simple single-day event.",
          startDate = base.plusSeconds(10 * 60 * 60),
          endDate = base.plusSeconds(12 * 60 * 60),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u1", "u2"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Blue),
      // e1) Recurring (weekly)
      Event(
          id = "e1",
          title = "Weekly stand-up",
          description = "Short, recurring meeting.",
          startDate = base.plus(day).plus(hour.multipliedBy(9)),
          endDate = base.plus(day).plus(hour.multipliedBy(10)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u3", "u4"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.Weekly,
          hasBeenDeleted = false,
          color = EventColor.Green),
      // e2) 3-day event
      Event(
          id = "e2",
          title = "Offsite (3 days)",
          description = "Team offsite spanning three days.",
          startDate = base.plus(day).plus(hour.multipliedBy(18)),
          endDate = base.plus(day.multipliedBy(4)).plus(hour.multipliedBy(18)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u2", "u5", "u6"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Orange),
      // e3) 3-day recurring
      Event(
          id = "e3",
          title = "Workshop series (3 days)",
          description = "Three-day recurring workshop.",
          startDate = base.plus(day.multipliedBy(2)).plus(hour.multipliedBy(8)),
          endDate = base.plus(day.multipliedBy(5)).plus(hour.multipliedBy(8)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u1", "u7"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.Weekly,
          hasBeenDeleted = false,
          color = EventColor.Purple),
      // e4) Long title
      Event(
          id = "e4",
          title =
              "Very long workshop title here with a lot of words making it intentionally too long for the card layout",
          description = "Long title showcase.",
          startDate = base.plus(day.multipliedBy(3)).plus(hour.multipliedBy(14)),
          endDate = base.plus(day.multipliedBy(3)).plus(hour.multipliedBy(16)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u2", "u8"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Black),
      // e5) Long description
      Event(
          id = "e5",
          title = "Deep-dive session",
          description =
              ("This is a very long description — " +
                      "it keeps going to test the collapse/expand behavior. ".repeat(8))
                  .trim(),
          startDate = base.plus(day.multipliedBy(4)).plus(hour.multipliedBy(10)),
          endDate = base.plus(day.multipliedBy(4)).plus(hour.multipliedBy(12)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u3", "u9"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Red),
      // e6) Many participants
      Event(
          id = "e6",
          title = "Large meeting",
          description = "A meeting with many attendees.",
          startDate = base.plus(hour.multipliedBy(13)),
          endDate = base.plus(hour.multipliedBy(15)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = (1..10).map { "u$it" }.toSet(),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Orange),
      // e7) Minimal
      Event(
          id = "e7",
          title = "Smallest event",
          description = "",
          startDate = base.plus(hour.multipliedBy(16)),
          endDate = base.plus(hour.multipliedBy(17)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = emptySet(),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Green))
}

@Preview(showBackground = true)
@Composable
private fun EventSummaryCardPreview_Weekly() {
  val names =
      mapOf(
          "u1" to "Alice",
          "u2" to "Bob",
          "u3" to "Charlie",
          "u4" to "David",
          "u5" to "Elvis",
          "u6" to "Franck",
          "u7" to "Gondulphe",
          "u8" to "Harry",
          "u9" to "Igor",
          "u10" to "Johan")

  // Change here the preview index for quick testing
  val event = previewEvents[0]
  // Event index correspondence :
  // 0: Simple single-day             (should display correctly)
  // 1: Weekly recurring              (should display the recurrence)
  // 2: 3-day event                   (should adapt the 2 lines of the date)
  // 3: 3-day event, weekly recurring (should combine both)
  // 4: Very long title               (should fade the last letters
  //                                        + show a working "show more/less" button)
  // 5: Very long description         (same : shade + responsive button)
  // 6: Many participants (10)        (should make the list scrollable with, and display only half
  //                                          of the last name visible to improve ux)
  // 7: Minimal                       (should render without crash, no description/participants)

  val participantDisplayNames = event.participants.mapNotNull { names[it] }

  EventSummaryCard(
      event = event, participantNames = participantDisplayNames, modifier = Modifier.fillMaxWidth())
}
