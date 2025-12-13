package com.android.sample.model.eventRepositoryTest

import com.android.sample.data.firebase.repositories.EventRepositoryFirebase
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.constants.FirestoreConstants
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.time.Instant
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepositoryFirebaseTest {

  private val firestore: FirebaseFirestore = mock()
  private val organizationsCollection: CollectionReference = mock()
  private val eventsCollection: CollectionReference = mock()
  private val organizationDocument: DocumentReference = mock()
  private val eventDocument: DocumentReference = mock()
  private val organizationSnapshot: DocumentSnapshot = mock()

  private lateinit var repository: EventRepositoryFirebase

  private val orgId = "org-1"

  @Before
  fun setUp() {
    repository = EventRepositoryFirebase(firestore)

    whenever(firestore.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH))
        .thenReturn(organizationsCollection)
    whenever(organizationsCollection.document(any())).thenReturn(organizationDocument)
    whenever(organizationDocument.collection(FirestoreConstants.EVENTS_COLLECTION_PATH))
        .thenReturn(eventsCollection)

    whenever(firestore.collection(FirestoreConstants.EVENTS_COLLECTION_PATH))
        .thenReturn(eventsCollection)
    whenever(eventsCollection.document()).thenReturn(eventDocument)
    whenever(eventsCollection.document(any())).thenReturn(eventDocument)
  }

  @Test
  fun getNewUid_returnsIdFromFirestore() {
    whenever(eventDocument.id).thenReturn("generated-id")

    val newId = repository.getNewUid()

    assertEquals("generated-id", newId)
  }

  @Test
  fun getEventsBetweenDates_invalidRange_throws() = runTest {
    val start = Instant.parse("2024-04-10T10:00:00Z")
    val end = Instant.parse("2024-04-09T10:00:00Z")

    assertFailsWith<IllegalArgumentException> {
      repository.getEventsBetweenDates(orgId, start, end)
    }
  }

  @Test
  fun getEventById_deletedEvent_returnsNull() = runTest {
    whenever(eventDocument.get()).thenReturn(Tasks.forResult(organizationSnapshot))
    whenever(organizationSnapshot.exists()).thenReturn(true)
    whenever(organizationSnapshot.getBoolean("hasBeenDeleted")).thenReturn(true)

    val result = repository.getEventById(orgId, "event-1")

    assertNull(result)
  }

  @Test
  fun getEventById_validEvent_returnsMappedEvent() = runTest {
    val start = Instant.parse("2024-05-01T10:00:00Z")
    val end = Instant.parse("2024-05-01T12:00:00Z")
    val snapshot =
        createEventSnapshot(
            id = "event-2",
            organizationId = orgId,
            title = "Planning",
            description = "Sprint planning",
            startDate = start,
            endDate = end,
            participants = setOf("user-a"),
            presence = mapOf("user-a" to true))

    whenever(eventDocument.get()).thenReturn(Tasks.forResult(snapshot))

    val result = repository.getEventById(orgId, "event-2")

    assertNotNull(result)
    assertEquals("Planning", result.title)
    assertEquals(start, result.startDate)
    assertEquals(end, result.endDate)
  }

  @Test
  fun getEventsBetweenDates_filtersOutEventsStartingAfterRangeEnd() = runTest {
    val start = Instant.parse("2024-06-01T00:00:00Z")
    val end = Instant.parse("2024-06-30T23:59:59Z")
    val withinRange =
        createEventSnapshot(
            id = "within",
            organizationId = orgId,
            title = "Review",
            description = "Quarterly review",
            startDate = Instant.parse("2024-06-15T10:00:00Z"),
            endDate = Instant.parse("2024-06-15T11:00:00Z"))
    val afterRange =
        createEventSnapshot(
            id = "after",
            organizationId = orgId,
            title = "Follow-up",
            description = "Post review",
            startDate = Instant.parse("2024-07-01T10:00:00Z"),
            endDate = Instant.parse("2024-07-01T11:00:00Z"))

    val querySnapshot =
        mock<QuerySnapshot> {
          on { documents } doReturn listOf(withinRange, afterRange)
          on { iterator() } doReturn mutableListOf(withinRange, afterRange).iterator()
        }

    val query: Query = mock()
    whenever(eventsCollection.whereGreaterThanOrEqualTo(eq("endDate"), any())).thenReturn(query)
    whenever(query.get()).thenReturn(Tasks.forResult(querySnapshot))

    val results = repository.getEventsBetweenDates(orgId, start, end)

    assertEquals(listOf("within"), results.map { it.id })
  }

  @Test
  fun calculateWorkedHoursPastEvents_missingOrganization_throws() = runTest {
    val repoSpy = spy(repository)
    doReturn(emptyList<Event>()).whenever(repoSpy).getEventsBetweenDates(eq(orgId), any(), any())

    whenever(organizationSnapshot.exists()).thenReturn(false)
    whenever(organizationDocument.get()).thenReturn(Tasks.forResult(organizationSnapshot))

    assertFailsWith<IllegalArgumentException> {
      repoSpy.calculateWorkedHoursPastEvents(
          orgId = orgId,
          start = Instant.parse("2024-01-01T00:00:00Z"),
          end = Instant.parse("2024-12-31T23:59:59Z"))
    }
  }

  @Test
  fun calculateWorkedHoursPastEvents_countsOnlyPresentParticipants() = runTest {
    val now = Instant.now()
    val pastEvent =
        createEvent(
            organizationId = orgId,
            title = "Workshop",
            startDate = now.minusSeconds(7200),
            endDate = now.minusSeconds(3600),
            cloudStorageStatuses = emptySet(),
            participants = setOf("present-user"),
            presence = mapOf("present-user" to true))[0]
    val absentEvent =
        createEvent(
            organizationId = orgId,
            title = "Optional",
            startDate = now.minusSeconds(3600),
            endDate = now,
            cloudStorageStatuses = emptySet(),
            participants = setOf("absent-user"),
            presence = mapOf("absent-user" to false))[0]

    val repoSpy = spy(repository)
    doReturn(listOf(pastEvent, absentEvent))
        .whenever(repoSpy)
        .getEventsBetweenDates(eq(orgId), any(), any())

    whenever(organizationSnapshot.exists()).thenReturn(true)
    whenever(organizationDocument.get()).thenReturn(Tasks.forResult(organizationSnapshot))

    val workedHours =
        repoSpy.calculateWorkedHoursPastEvents(
            orgId = orgId, start = now.minusSeconds(10_000), end = now.plusSeconds(1))

    assertEquals(listOf("present-user" to 1.0), workedHours)
  }

  @Test
  fun calculateWorkedHoursFutureEvents_countsAllParticipants() = runTest {
    val now = Instant.now()
    val futureEvent =
        createEvent(
            organizationId = orgId,
            title = "Shift",
            startDate = now.plusSeconds(3600),
            endDate = now.plusSeconds(7200),
            cloudStorageStatuses = emptySet(),
            participants = setOf("alice", "bob"))[0]

    val repoSpy = spy(repository)
    doReturn(listOf(futureEvent)).whenever(repoSpy).getEventsBetweenDates(eq(orgId), any(), any())

    whenever(organizationSnapshot.exists()).thenReturn(true)
    whenever(organizationDocument.get()).thenReturn(Tasks.forResult(organizationSnapshot))

    val workedHours =
        repoSpy.calculateWorkedHoursFutureEvents(
            orgId = orgId, start = now, end = now.plusSeconds(86_400))

    assertEquals(setOf("alice" to 1.0, "bob" to 1.0), workedHours.toSet())
  }

  private fun createEventSnapshot(
      id: String,
      organizationId: String,
      title: String,
      description: String,
      startDate: Instant,
      endDate: Instant,
      participants: Set<String> = emptySet(),
      presence: Map<String, Boolean> = emptyMap()
  ): QueryDocumentSnapshot {
    val data: Map<String, Any?> =
        mapOf(
            "id" to id,
            "organizationId" to organizationId,
            "title" to title,
            "description" to description,
            "startDate" to Timestamp(Date.from(startDate)),
            "endDate" to Timestamp(Date.from(endDate)),
            "participants" to participants.toList(),
            "storageStatus" to listOf("FIRESTORE"),
            "recurrenceStatus" to "OneTime",
            "presence" to presence,
            "version" to 0L,
            "personalNotes" to null,
            "eventColor" to null)

    val snapshot: QueryDocumentSnapshot = mock()

    whenever(snapshot.getId()).thenReturn(id)
    whenever(snapshot.id).thenReturn(id)
    whenever(snapshot.exists()).thenReturn(true)
    whenever(snapshot.getBoolean("hasBeenDeleted")).thenReturn(false)

    whenever(snapshot.getString(any())).thenAnswer { invocation ->
      val key = invocation.arguments[0] as String
      data[key] as? String
    }
    whenever(snapshot.getTimestamp(any())).thenAnswer { invocation ->
      val key = invocation.arguments[0] as String
      data[key] as? Timestamp
    }
    whenever(snapshot.get(any<String>())).thenAnswer { invocation ->
      val key = invocation.arguments[0] as String
      data[key]
    }
    whenever(snapshot.getLong(any())).thenAnswer { invocation ->
      val key = invocation.arguments[0] as String
      (data[key] as? Number)?.toLong()
    }

    return snapshot
  }

  @Test
  fun ongoingEvent_isCountedAsPastNotFuture() = runTest {
    val now = Instant.now()

    // Ongoing event: started 30 minutes ago, ends in 30 minutes → FULL duration is 1h
    val ongoingEvent =
        createEvent(
            organizationId = orgId,
            title = "OngoingShift",
            startDate = now.minusSeconds(1800),
            endDate = now.plusSeconds(1800),
            cloudStorageStatuses = emptySet(),
            participants = setOf("worker"),
            presence = mapOf("worker" to true))[0]

    val repoSpy = spy(repository)

    // Mock event retrieval
    doReturn(listOf(ongoingEvent)).whenever(repoSpy).getEventsBetweenDates(eq(orgId), any(), any())

    whenever(organizationSnapshot.exists()).thenReturn(true)
    whenever(organizationDocument.get()).thenReturn(Tasks.forResult(organizationSnapshot))

    // 1) Past events MUST include ongoing event (full duration)
    val pastHours =
        repoSpy.calculateWorkedHoursPastEvents(
            orgId = orgId, start = now.minusSeconds(10_000), end = now.plusSeconds(1))

    // 2) Future events MUST NOT include it
    val futureHours =
        repoSpy.calculateWorkedHoursFutureEvents(
            orgId = orgId, start = now.minusSeconds(1), end = now.plusSeconds(10_000))

    // Assert: counted as past
    assertEquals(
        listOf("worker" to 1.0),
        pastHours,
        "Ongoing event must be counted in past events with full duration")

    // Assert: NOT counted as future
    assertEquals(emptyList(), futureHours, "Ongoing event must NOT be counted in future events")
  }
}
