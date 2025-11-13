package com.android.sample.model.replacement

import com.android.sample.model.calendar.Event
import java.util.UUID

/** Represents the current status of a replacement request */
enum class ReplacementStatus {
  ToProcess,
  WaitingForAnswer,
  Accepted,
  Declined
}

/**
 * Represents a replacement request for a specific event
 *
 * @property id Unique identifier of the replacement request
 * @property absentUserId ID of the member who needs to be replaced
 * @property substituteUserId ID of the member proposed as the substitute
 * @property event Event for which the replacement is requested
 * @property status Current status of the replacement request
 */
data class Replacement(
    val id: String = UUID.randomUUID().toString(),
    val absentUserId: String,
    val substituteUserId: String,
    val event: Event,
    val status: ReplacementStatus = ReplacementStatus.ToProcess
)
