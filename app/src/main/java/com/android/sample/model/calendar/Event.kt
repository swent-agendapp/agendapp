package com.android.sample.model.calendar

import java.time.Instant

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
)

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
 * @return A new Event instance.
 */
fun createEvent(
    title: String,
    description: String,
    startDate: Instant,
    endDate: Instant,
    cloudStorageStatuses: Set<CloudStorageStatus> = emptySet(),
    personalNotes: String? = null,
    participants: Set<String> = emptySet()
): Event {
  return Event(
      id = java.util.UUID.randomUUID().toString(),
      title = title,
      description = description,
      startDate = startDate,
      endDate = endDate,
      cloudStorageStatuses = cloudStorageStatuses,
      personalNotes = personalNotes,
      participants = participants,
      version = System.currentTimeMillis(),
      recurrenceStatus = RecurrenceStatus.OneTime)
}

fun RecurrenceStatus.formatString(): String =
    when (this) {
      RecurrenceStatus.OneTime -> "One Time"
      RecurrenceStatus.Weekly -> "Weekly"
      RecurrenceStatus.Monthly -> "Monthly"
      RecurrenceStatus.Yearly -> "Yearly"
    }
