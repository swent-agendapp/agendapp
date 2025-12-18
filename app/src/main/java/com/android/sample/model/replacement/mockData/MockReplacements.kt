package com.android.sample.model.replacement.mockData

import com.android.sample.model.authentication.User
import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.category.EventCategory
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import java.time.Instant

const val WEIFENG = "weifeng"
const val TIMAEL = "timael"
const val HAOBIN = "haobin"
const val ALICE = "alice"
const val BOB = "bob"
const val EMILIEN = "emilien"

fun getMockReplacements(): List<Replacement> {
  val eventA =
      createEvent(
          organizationId = "orgTest",
          title = "Reunion A",
          description = "Morning meeting",
          startDate = Instant.parse("2025-10-14T08:00:00Z"),
          endDate = Instant.parse("2025-10-14T10:45:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          category = EventCategory.defaultCategory())

  val eventB =
      createEvent(
          organizationId = "orgTest",
          title = "Reunion B",
          description = "Late morning meeting",
          startDate = Instant.parse("2025-10-18T09:15:00Z"),
          endDate = Instant.parse("2025-10-18T13:45:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          category = EventCategory.defaultCategory())

  val eventC =
      createEvent(
          organizationId = "orgTest",
          title = "Reunion C",
          description = "Afternoon meeting",
          startDate = Instant.parse("2025-12-03T14:00:00Z"),
          endDate = Instant.parse("2025-12-03T16:00:00Z"),
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          category = EventCategory.defaultCategory())

  return listOf(
      Replacement(
          id = "r1",
          absentUserId = HAOBIN,
          substituteUserId = TIMAEL,
          event = eventA[0],
          status = ReplacementStatus.ToProcess),
      Replacement(
          id = "r2",
          absentUserId = WEIFENG,
          substituteUserId = TIMAEL,
          event = eventB[0],
          status = ReplacementStatus.WaitingForAnswer),
      Replacement(
          id = "r3",
          absentUserId = EMILIEN,
          substituteUserId = TIMAEL,
          event = eventC[0],
          status = ReplacementStatus.Accepted),
      Replacement(
          id = "r4",
          absentUserId = BOB,
          substituteUserId = ALICE,
          event = eventB[0],
          status = ReplacementStatus.Declined))
}

fun getMockUsers(): List<User> =
    listOf(
        User(WEIFENG, WEIFENG),
        User(EMILIEN, EMILIEN),
        User(HAOBIN, HAOBIN),
        User(ALICE, ALICE),
        User(BOB, BOB),
        User(TIMAEL, TIMAEL),
    )
