package com.android.sample.model.replacement.mockData

import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.utils.EventColor
import java.time.Instant

fun getMockReplacements(): List<Replacement> {
  val eventA =
      createEvent(
          title = "Reunion A",
          description = "Morning meeting",
          startDate = Instant.parse("2025-10-14T08:00:00Z"),
          endDate = Instant.parse("2025-10-14T10:45:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          color = EventColor.Blue)

  val eventB =
      createEvent(
          title = "Reunion B",
          description = "Late morning meeting",
          startDate = Instant.parse("2025-10-18T09:15:00Z"),
          endDate = Instant.parse("2025-10-18T13:45:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          color = EventColor.Green)

  val eventC =
      createEvent(
          title = "Reunion C",
          description = "Afternoon meeting",
          startDate = Instant.parse("2025-11-03T14:00:00Z"),
          endDate = Instant.parse("2025-11-03T16:00:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          color = EventColor.Orange)

  return listOf(
      Replacement(
          id = "r1",
          absentUserId = "emilien.barde@epfl.ch",
          substituteUserId = "noa.floret@epfl.ch",
          event = eventA,
          status = ReplacementStatus.Pending),
      Replacement(
          id = "r2",
          absentUserId = "weifeng.ding@epfl.ch",
          substituteUserId = "timael.andrie@epfl.ch",
          event = eventB,
          status = ReplacementStatus.Pending),
      Replacement(
          id = "r3",
          absentUserId = "emilien.barde@epfl.ch",
          substituteUserId = "timael.andrie@epfl.ch",
          event = eventC,
          status = ReplacementStatus.Accepted))
}
