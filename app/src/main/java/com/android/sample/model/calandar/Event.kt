package com.android.sample.model.calandar

import java.time.Instant

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Instant,   // Format: "dd-MM-yyyy"
    val endDate: Instant,     // Format: "dd-MM-yyyy"
    val status: Set<EventStatus> = emptySet(),
    val personalNotes: String? = null
)

enum class EventStatus {
    LOCAL,
    FIRESTORE,
}

fun createEvent(
    title: String,
    description: String,
    startDate: Instant,
    endDate: Instant,
    status: EventStatus,
    personalNotes: String? = null
): Event {
    return Event(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        description = description,
        startDate = startDate,
        endDate = endDate,
        status =setOf(status),
        personalNotes = personalNotes
    )
}