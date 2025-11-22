package com.android.sample.model.replacement.mockData

import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.ui.theme.EventPalette
import java.time.Instant

const val DEFAULT_EMAIL = "timael.andrie@epfl.ch"

fun getMockReplacements(): List<Replacement> {
  val eventA =
      createEvent(
          organizationId = "orgTest",
          title = "Reunion A",
          description = "Morning meeting",
          startDate = Instant.parse("2025-10-14T08:00:00Z"),
          endDate = Instant.parse("2025-10-14T10:45:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          color = EventPalette.Blue)

  val eventB =
      createEvent(
          organizationId = "orgTest",
          title = "Reunion B",
          description = "Late morning meeting",
          startDate = Instant.parse("2025-10-18T09:15:00Z"),
          endDate = Instant.parse("2025-10-18T13:45:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          color = EventPalette.Green)

  val eventC =
      createEvent(
          organizationId = "orgTest",
          title = "Reunion C",
          description = "Afternoon meeting",
          startDate = Instant.parse("2025-11-03T14:00:00Z"),
          endDate = Instant.parse("2025-11-03T16:00:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          color = EventPalette.Orange)

  return listOf(
      Replacement(
          id = "r1",
          absentUserId = "haobin.wang@epfl.ch",
          substituteUserId = DEFAULT_EMAIL,
          event = eventA[0],
          status = ReplacementStatus.ToProcess),
      Replacement(
          id = "r2",
          absentUserId = "weifeng.ding@epfl.ch",
          substituteUserId = DEFAULT_EMAIL,
          event = eventB[0],
          status = ReplacementStatus.WaitingForAnswer),
      Replacement(
          id = "r3",
          absentUserId = "emilien.barde@epfl.ch",
          substituteUserId = DEFAULT_EMAIL,
          event = eventC[0],
          status = ReplacementStatus.Accepted),
      Replacement(
          id = "r4",
          absentUserId = "bob@epfl.ch",
          substituteUserId = "alice@epfl.ch",
          event = eventB[0],
          status = ReplacementStatus.Declined))
}
