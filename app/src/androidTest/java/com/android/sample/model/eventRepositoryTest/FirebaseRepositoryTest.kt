package com.android.sample.model.eventRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.StorageStatus
import com.android.sample.model.calendar.createEvent
import com.android.sample.utils.FirebaseEmulatedTest
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class FirebaseRepositoryTest : FirebaseEmulatedTest() {

  private lateinit var repository: EventRepository
  private lateinit var event1: Event
  private lateinit var event2: Event

  @Before
  override fun setUp() {

    super.setUp()
    repository = createInitializedRepository()

    event1 =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            storageStatus = setOf(StorageStatus.LOCAL),
            personalNotes = "Bring laptop")

    event2 =
        createEvent(
            title = "Conference",
            description = "Tech event",
            startDate = Instant.parse("2025-02-01T09:00:00Z"),
            endDate = Instant.parse("2025-02-03T18:00:00Z"),
            storageStatus = setOf(StorageStatus.FIRESTORE))
  }

  @Test
  fun insertEvent_andGetById_shouldWork() = runBlocking {
    repository.insertEvent(event1)
    val retrieved = repository.getEventById(event1.id)
    Assert.assertNotNull(retrieved)
    Assert.assertEquals(event1.title, retrieved?.title)
  }

  @Test
  fun getAllEvents_shouldReturnInsertedOnes() = runBlocking {
    repository.insertEvent(event1)
    repository.insertEvent(event2)
    val allEvents = repository.getAllEvents()
    Assert.assertEquals(2, allEvents.size)
  }

  @Test
  fun updateEvent_shouldReplaceExistingEvent() = runBlocking {
    repository.insertEvent(event1)
    val updated = event1.copy(title = "Updated Meeting")
    repository.updateEvent(event1.id, updated)
    val retrieved = repository.getEventById(event1.id)
    Assert.assertEquals("Updated Meeting", retrieved?.title)
  }

  @Test
  fun deleteEvent_shouldRemoveEvent() = runBlocking {
    repository.insertEvent(event1)
    repository.deleteEvent(event1.id)
    Assert.assertNull(repository.getEventById(event1.id))
  }

  @Test
  fun getEventsBetweenDates_shouldReturnEventsWithinRange() = runBlocking {
    repository.insertEvent(event1)
    repository.insertEvent(event2)

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-31T23:59:59Z"))

    Assert.assertEquals(1, results.size)
    Assert.assertEquals(event1.id, results.first().id)
  }

  @Test
  fun getEventsBetweenDates_shouldThrowIllegalArgumentExceptionForInvalidRange() = runBlocking {
    repository.insertEvent(event1)
    repository.insertEvent(event2)

    try {
      repository.getEventsBetweenDates(
          Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-01-01T23:59:59Z"))
      Assert.fail("Expected IllegalArgumentException for invalid date range")
    } catch (_: IllegalArgumentException) {
      // Success: IllegalArgumentException exception thrown as expected
    }
  }

  @Test
  fun getAllUnsyncedEvents_shouldReturnEventsNotSyncedToGivenDb() = runBlocking {
    repository.insertEvent(event1)
    repository.insertEvent(event2)

    val unsyncedToFirestore = repository.getAllUnsyncedEvents(StorageStatus.FIRESTORE)
    Assert.assertTrue(unsyncedToFirestore.any { it.id == event1.id })
    Assert.assertFalse(unsyncedToFirestore.any { it.id == event2.id })
  }

  @Test
  fun documentToEvent_customStorageStatus_shouldParse() = runBlocking {
    val customEvent =
        createEvent(
            title = "MultiStorage Event",
            description = "Testing storage status parsing",
            startDate = Instant.parse("2025-01-01T10:00:00Z"),
            endDate = Instant.parse("2025-01-01T11:00:00Z"),
            storageStatus = setOf(StorageStatus.LOCAL, StorageStatus.FIRESTORE),
            personalNotes = "None",
            owners = setOf("Alice", "Bob"),
            participants = setOf("Charlie"))

    repository.insertEvent(customEvent)

    val retrieved = repository.getEventById(customEvent.id)

    Assert.assertNotNull(retrieved)
    Assert.assertTrue(
        retrieved!!.storageStatus.containsAll(listOf(StorageStatus.LOCAL, StorageStatus.FIRESTORE)))
    Assert.assertEquals(2, retrieved.storageStatus.size)
  }

  @Test
  fun documentToEvent_shouldHandleNullOptionalFields() = runBlocking {
    val eventWithMissingOptional =
        createEvent(
            title = "No description",
            description = "",
            startDate = Instant.parse("2025-03-01T10:00:00Z"),
            endDate = Instant.parse("2025-03-01T11:00:00Z"),
            storageStatus = emptySet(),
            personalNotes = null,
            owners = emptySet(),
            participants = emptySet())

    repository.insertEvent(eventWithMissingOptional)

    val retrieved = repository.getEventById(eventWithMissingOptional.id)
    Assert.assertNotNull(retrieved)
    Assert.assertEquals("", retrieved!!.description)
    Assert.assertEquals(emptySet<String>(), retrieved.owners)
    Assert.assertEquals(emptySet<String>(), retrieved.participants)
  }
}
