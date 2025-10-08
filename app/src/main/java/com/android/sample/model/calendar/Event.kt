package com.android.sample.model.calendar

import java.time.Instant

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Instant, // Format: "dd-MM-yyyy"
    val endDate: Instant, // Format: "dd-MM-yyyy"
    val storageStatus: Set<StorageStatus>,
    val personalNotes: String?,
    val owners: Set<String>,
    val participants: Set<String>,
    val version: Long,
    val recurrenceStatus: RecurrenceStatus
)

enum class RecurrenceStatus {
  OneTime,
  Weekly,
  Monthly,
  Yearly
}

enum class StorageStatus {
  LOCAL,
  FIRESTORE,
}

fun createEvent(
    title: String,
    description: String,
    startDate: Instant,
    endDate: Instant,
    storageStatus: Set<StorageStatus>,
    personalNotes: String? = null,
    // Old line :
    // owners: Set<String> = FirebaseAuth.getInstance().currentUser?.uid,
    // Temporary line, just for testing purposes
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
