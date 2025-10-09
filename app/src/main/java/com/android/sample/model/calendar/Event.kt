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
 * @property storageStatus Set of storage locations where the event is saved (e.g., local, Firestore).
 * @property personalNotes Optional personal notes for the event.
 * @property owners Set of user IDs who own the event.
 * @property participants Set of user IDs participating in the event.
 * @property version Version number for concurrency control.
 * @property recurrenceStatus Recurrence pattern of the event (e.g., one-time, weekly).
 */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Instant,
    val endDate: Instant,
    val storageStatus: Set<StorageStatus>,
    val personalNotes: String?,
    val owners: Set<String>,
    val participants: Set<String>,
    val version: Long,
    val recurrenceStatus: RecurrenceStatus
)

/**
 * Enum representing the recurrence pattern of an event.
 */
enum class RecurrenceStatus {
  OneTime,
  Weekly,
  Monthly,
  Yearly
}

/**
 * Enum representing the storage location of an event.
 */
enum class StorageStatus {
  LOCAL,
  FIRESTORE,
}

/**
 * Factory function to create a new Event instance with a generated unique ID and default values.
 *
 * @param title Title of the event.
 * @param description Description of the event.
 * @param startDate Start date and time of the event.
 * @param endDate End date and time of the event.
 * @param storageStatus Set of storage locations for the event.
 * @param personalNotes Optional personal notes.
 * @param owners Set of user IDs who own the event.
 * @param participants Set of user IDs participating in the event.
 * @return A new Event instance.
 */
fun createEvent(
    title: String,
    description: String,
    startDate: Instant,
    endDate: Instant,
    storageStatus: Set<StorageStatus>,
    personalNotes: String? = null,
    owners: Set<String> = emptySet(),
    participants: Set<String> = emptySet()
): Event {
  return Event(
      id = java.util.UUID.randomUUID().toString(),
      title = title,
      description = description,
      startDate = startDate,
      endDate = endDate,
      storageStatus = storageStatus,
      personalNotes = personalNotes,
      owners = owners,
      participants = participants,
      version = 0L,
      recurrenceStatus = RecurrenceStatus.OneTime)
}
