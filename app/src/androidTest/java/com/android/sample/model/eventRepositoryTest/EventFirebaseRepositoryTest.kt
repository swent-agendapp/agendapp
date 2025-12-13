package com.android.sample.model.eventRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepositoryProvider.repository
import com.android.sample.model.calendar.createEvent
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class EventFirebaseRepositoryTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  private lateinit var event1: Event
  private lateinit var event2: Event
  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  override fun setUp() {
    super.setUp()

    event1 =
        createEvent(
            organizationId = organizationId,
            title = "Meeting",
            description = "Team sync",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
            personalNotes = "Bring laptop")[0]

    event2 =
        createEvent(
            organizationId = organizationId,
            title = "Conference",
            description = "Tech event",
            startDate = Instant.parse("2025-02-01T09:00:00Z"),
            endDate = Instant.parse("2025-02-03T18:00:00Z"),
            cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE))[0]
  }

  @Test
  fun insertEvent_andGetById_shouldWork() = runBlocking {
    repository.insertEvent(orgId = organizationId, item = event1)
    val retrieved = repository.getEventById(orgId = organizationId, itemId = event1.id)
    Assert.assertNotNull(retrieved)
    Assert.assertEquals(event1.title, retrieved?.title)
  }

  @Test
  fun getAllEvents_shouldReturnInsertedOnes() = runBlocking {
    repository.insertEvent(orgId = organizationId, item = event1)
    repository.insertEvent(orgId = organizationId, item = event2)
    val allEvents = repository.getAllEvents(orgId = organizationId)
    Assert.assertEquals(2, allEvents.size)
  }

  @Test
  fun updateEvent_shouldReplaceExistingEvent() = runBlocking {
    repository.insertEvent(orgId = organizationId, item = event1)
    val updated = event1.copy(title = "Updated Meeting")
    repository.updateEvent(orgId = organizationId, itemId = event1.id, item = updated)
    val retrieved = repository.getEventById(orgId = organizationId, itemId = event1.id)
    Assert.assertEquals("Updated Meeting", retrieved?.title)
  }

  @Test
  fun deleteEvent_shouldRemoveEvent() = runBlocking {
    repository.insertEvent(orgId = organizationId, item = event1)
    repository.deleteEvent(orgId = organizationId, itemId = event1.id)
    Assert.assertNull(repository.getEventById(orgId = organizationId, itemId = event1.id))
  }

  @Test
  fun getEventsBetweenDates_returnsEventsFullyInsideRange() = runBlocking {
    // event fully inside the range should be returned
    repository.insertEvent(
        orgId = organizationId,
        item =
            event1.copy(
                id = "in-range",
                startDate = Instant.parse("2025-02-10T10:00:00Z"),
                endDate = Instant.parse("2025-02-10T11:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            orgId = organizationId,
            startDate = Instant.parse("2025-02-01T00:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z"))

    Assert.assertEquals(1, results.size)
    Assert.assertEquals("in-range", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_excludesEventsBeforeRange() = runBlocking {
    // event ending before the start of the range should be ignored
    repository.insertEvent(
        orgId = organizationId,
        item =
            event1.copy(
                id = "before-range",
                startDate = Instant.parse("2025-01-31T09:00:00Z"),
                endDate = Instant.parse("2025-01-31T10:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            orgId = organizationId,
            startDate = Instant.parse("2025-02-01T00:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z"))

    Assert.assertTrue(results.isEmpty())
  }

  @Test
  fun getEventsBetweenDates_excludesEventsAfterRange() = runBlocking {
    // event starting after the end of the range should be ignored
    repository.insertEvent(
        orgId = organizationId,
        item =
            event1.copy(
                id = "after-range",
                startDate = Instant.parse("2025-03-01T09:00:00Z"),
                endDate = Instant.parse("2025-03-01T10:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            orgId = organizationId,
            startDate = Instant.parse("2025-02-01T00:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z"))

    Assert.assertTrue(results.isEmpty())
  }

  @Test
  fun getEventsBetweenDates_includesEventsOverlappingStart() = runBlocking {
    // event starting before but ending inside the range should be returned
    repository.insertEvent(
        orgId = organizationId,
        item =
            event1.copy(
                id = "overlap-start",
                startDate = Instant.parse("2025-01-31T23:00:00Z"),
                endDate = Instant.parse("2025-02-01T01:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            orgId = organizationId,
            startDate = Instant.parse("2025-02-01T00:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z"))

    Assert.assertEquals(1, results.size)
    Assert.assertEquals("overlap-start", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_includesEventsOverlappingEnd() = runBlocking {
    // event starting inside but ending after the range should be returned
    repository.insertEvent(
        orgId = organizationId,
        item =
            event1.copy(
                id = "overlap-end",
                startDate = Instant.parse("2025-02-28T22:00:00Z"),
                endDate = Instant.parse("2025-03-01T01:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            orgId = organizationId,
            startDate = Instant.parse("2025-02-01T00:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z"))

    Assert.assertEquals(1, results.size)
    Assert.assertEquals("overlap-end", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_includesEventsEndingExactlyAtStart() = runBlocking {
    // event ending exactly at the start of the range should be returned
    repository.insertEvent(
        orgId = organizationId,
        item =
            event1.copy(
                id = "end-at-start",
                startDate = Instant.parse("2025-01-31T22:00:00Z"),
                endDate = Instant.parse("2025-02-01T00:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            orgId = organizationId,
            startDate = Instant.parse("2025-02-01T00:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z"))

    Assert.assertEquals(1, results.size)
    Assert.assertEquals("end-at-start", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_includesEventsEndingExactlyAtEnd() = runBlocking {
    // event ending exactly at the end of the range should be returned
    repository.insertEvent(
        orgId = organizationId,
        item =
            event1.copy(
                id = "end-at-end",
                startDate = Instant.parse("2025-02-28T22:00:00Z"),
                endDate = Instant.parse("2025-02-28T23:59:59Z")))

    val results =
        repository.getEventsBetweenDates(
            orgId = organizationId,
            startDate = Instant.parse("2025-02-01T00:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z"))

    Assert.assertEquals(1, results.size)
    Assert.assertEquals("end-at-end", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_includesEventsCoveringWholeRange() = runBlocking {
    // event that starts before and ends after the range should be returned
    repository.insertEvent(
        orgId = organizationId,
        item =
            event1.copy(
                id = "covering",
                startDate = Instant.parse("2025-01-01T00:00:00Z"),
                endDate = Instant.parse("2025-03-01T00:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            orgId = organizationId,
            startDate = Instant.parse("2025-02-01T00:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z"))

    Assert.assertEquals(1, results.size)
    Assert.assertEquals("covering", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_shouldThrowIllegalArgumentExceptionForInvalidRange() = runBlocking {
    repository.insertEvent(orgId = organizationId, item = event1)
    repository.insertEvent(orgId = organizationId, item = event2)

    try {
      repository.getEventsBetweenDates(
          orgId = organizationId,
          startDate = Instant.parse("2025-02-01T00:00:00Z"),
          endDate = Instant.parse("2025-01-01T23:59:59Z"))
      Assert.fail("Expected IllegalArgumentException for invalid date range")
    } catch (_: IllegalArgumentException) {
      // Success: IllegalArgumentException exception thrown as expected
    }
  }

  @Test
  fun getEventById_returnsEvent_whenExists() = runBlocking {
    val inserted =
        createEvent(
            organizationId = organizationId,
            title = "Board meeting",
            description = "Quarterly review",
            startDate = Instant.parse("2025-04-10T08:00:00Z"),
            endDate = Instant.parse("2025-04-10T09:30:00Z"),
            cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
            personalNotes = "Slides in drive")[0]

    // Insert the event into the repository
    repository.insertEvent(orgId = organizationId, item = inserted)

    val retrieved = repository.getEventById(orgId = organizationId, itemId = inserted.id)

    // The event should exists with exact same fields as created
    Assert.assertNotNull("Expected to retrieve an existing event by id", retrieved)
    Assert.assertEquals(inserted.id, retrieved!!.id)
    Assert.assertEquals("Board meeting", retrieved.title)
    Assert.assertEquals(Instant.parse("2025-04-10T08:00:00Z"), retrieved.startDate)
    Assert.assertEquals(Instant.parse("2025-04-10T09:30:00Z"), retrieved.endDate)
  }

  @Test
  fun getEventById_returnsNull_whenMissing() = runBlocking {
    val missing = repository.getEventById(orgId = organizationId, itemId = "no-such-event-id")
    Assert.assertNull("Unknown id should yield null", missing)
  }

  @Test
  fun documentToEvent_customStorageStatus_shouldParse() = runBlocking {
    val customEvent =
        createEvent(
            organizationId = organizationId,
            title = "MultiStorage Event",
            description = "Testing storage status parsing",
            startDate = Instant.parse("2025-01-01T10:00:00Z"),
            endDate = Instant.parse("2025-01-01T11:00:00Z"),
            cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
            personalNotes = "None",
            participants = setOf("Charlie"))[0]

    repository.insertEvent(orgId = organizationId, item = customEvent)

    val retrieved = repository.getEventById(orgId = organizationId, itemId = customEvent.id)

    Assert.assertNotNull(retrieved)
    Assert.assertTrue(
        retrieved!!.cloudStorageStatuses.containsAll(listOf(CloudStorageStatus.FIRESTORE)))
    Assert.assertEquals(1, retrieved.cloudStorageStatuses.size)
  }

  @Test
  fun documentToEvent_shouldHandleNullOptionalFields() = runBlocking {
    val eventWithMissingOptional =
        createEvent(
            organizationId = organizationId,
            title = "No description",
            description = "",
            startDate = Instant.parse("2025-03-01T10:00:00Z"),
            endDate = Instant.parse("2025-03-01T11:00:00Z"),
            cloudStorageStatuses = emptySet(),
            personalNotes = null,
            participants = emptySet())[0]

    repository.insertEvent(orgId = organizationId, item = eventWithMissingOptional)

    val retrieved =
        repository.getEventById(orgId = organizationId, itemId = eventWithMissingOptional.id)
    Assert.assertNotNull(retrieved)
    Assert.assertEquals("", retrieved!!.description)
    Assert.assertEquals(emptySet<String>(), retrieved.participants)
  }
}
