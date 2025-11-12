package com.android.sample.model.replacement

import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.createEvent
import com.android.sample.utils.EventColor
import com.google.common.truth.Truth
import java.time.Instant
import org.junit.Test

class ReplacementExtensionsTest {

  @Test
  fun pendingReplacements_returnsOnlyPendingItems() {
    val baseEvent =
        createEvent(
            title = "Test event",
            startDate = Instant.parse("2025-01-01T08:00:00Z"),
            endDate = Instant.parse("2025-01-01T10:00:00Z"),
            cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
            color = EventColor.Companion.Blue)

    val pending =
        Replacement(
            id = "1",
            absentUserId = "user-a",
            substituteUserId = "user-b",
            event = baseEvent,
            status = ReplacementStatus.Pending)

    val accepted =
        Replacement(
            id = "2",
            absentUserId = "user-c",
            substituteUserId = "user-d",
            event = baseEvent,
            status = ReplacementStatus.Accepted)

    val declined =
        Replacement(
            id = "3",
            absentUserId = "user-e",
            substituteUserId = "user-f",
            event = baseEvent,
            status = ReplacementStatus.Declined)

    val result = listOf(pending, accepted, declined).pendingReplacements()

    Truth.assertThat(result).containsExactly(pending)
  }
}
