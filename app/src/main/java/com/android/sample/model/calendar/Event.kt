package com.android.sample.model.calendar

import com.android.sample.utils.EventColor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Data class representing a calendar event.
 *
 * @property id Unique identifier for the event.
 * @property title Title of the event.
 * @property description Description of the event.
 * @property startDate Start date and time of the event.
 * @property endDate End date and time of the event.
 * @property cloudStorageStatuses Set of storage locations where the event is saved (e.g., local,
 *   Firestore).
 * @property personalNotes Optional personal notes for the event.
 * @property participants Set of user IDs participating in the event.
 * @property version timestamp of last modification, used for conflict resolution.
 * @property hasBeenDeleted Flag indicating if the event has been deleted.
 * @property recurrenceStatus Recurrence pattern of the event (e.g., one-time, weekly).
 * @property color Color used to display the event in the UI.
 * @property locallyStoredBy List of user IDs who have the event stored locally.
 */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Instant,
    val endDate: Instant,
    val cloudStorageStatuses: Set<CloudStorageStatus>,
    val locallyStoredBy: List<String> = emptyList(),
    val personalNotes: String?,
    val participants: Set<String>,
    val version: Long,
    val recurrenceStatus: RecurrenceStatus,
    val hasBeenDeleted: Boolean = false,
    val color: EventColor
) {
    // Returns the start date as a LocalDate in the system's default time zone
    val startLocalDate: LocalDate
        get() = startDate.atZone(ZoneId.systemDefault()).toLocalDate()

    // Returns the end date as a LocalDate in the system's default time zone
    val endLocalDate: LocalDate
        get() = endDate.atZone(ZoneId.systemDefault()).toLocalDate()

    // Returns the start time as a LocalTime in the system's default time zone
    val startLocalTime: LocalTime
        get() = startDate.atZone(ZoneId.systemDefault()).toLocalTime()

    // Returns the end time as a LocalTime in the system's default time zone
    val endLocalTime: LocalTime
        get() = endDate.atZone(ZoneId.systemDefault()).toLocalTime()
}

/** Enum representing the recurrence pattern of an event. */
enum class RecurrenceStatus {
  OneTime,
  Weekly,
  Monthly,
  Yearly
}

/** Enum representing the cloud storage location of an event. */
enum class CloudStorageStatus {
  FIRESTORE,
}

/**
 * Factory function to create a new Event instance with a generated unique ID and default values.
 *
 * @param title Title of the event.
 * @param description Description of the event.
 * @param startDate Start date and time of the event.
 * @param endDate End date and time of the event.
 * @param cloudStorageStatuses Set of storage locations for the event.
 * @param personalNotes Optional personal notes.
 * @param participants Set of user IDs participating in the event.
 * @param color Color used to display the event in the UI.
 * @return A new Event instance.
 */
fun createEvent(
    title: String = "",
    description: String = "",
    startDate: Instant = Instant.now(),
    endDate: Instant = Instant.now(),
    cloudStorageStatuses: Set<CloudStorageStatus> = emptySet(),
    personalNotes: String? = null,
    participants: Set<String> = emptySet(),
    color: EventColor = EventColor.Blue
): Event {
  // Ensure the end date is not before the start date
  require(!endDate.isBefore(startDate)) { "End date cannot be before start date" }
  return Event(
      id = java.util.UUID.randomUUID().toString(), // Generate a unique ID for the event
      title = title,
      description = description,
      startDate = startDate,
      endDate = endDate,
      cloudStorageStatuses = cloudStorageStatuses,
      personalNotes = personalNotes,
      participants = participants,
      version = System.currentTimeMillis(),
      recurrenceStatus = RecurrenceStatus.OneTime,
      color = color)
}
