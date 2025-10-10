package com.android.sample.model.calendar

import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class LocalRepositoryTest {

  private lateinit var repository: EventRepositoryLocal
  private lateinit var event1: Event
  private lateinit var event2: Event

  @Before
  fun setUp() {
    repository = EventRepositoryLocal()
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
    assertNotNull(retrieved)
    assertEquals(event1.title, retrieved?.title)
  }

  @Test
  fun getAllEvents_shouldReturnInsertedOnes() = runBlocking {
    repository.insertEvent(event1)
    repository.insertEvent(event2)
    val allEvents = repository.getAllEvents()
    assertEquals(2, allEvents.size)
  }

  @Test
  fun updateEvent_shouldReplaceExistingEvent() = runBlocking {
    repository.insertEvent(event1)
    val updated = event1.copy(title = "Updated Meeting")
    repository.updateEvent(event1.id, updated)
    val retrieved = repository.getEventById(event1.id)
    assertEquals("Updated Meeting", retrieved?.title)
  }

  @Test
  fun deleteEvent_shouldRemoveEvent() = runBlocking {
    repository.insertEvent(event1)
    repository.deleteEvent(event1.id)
    assertNull(repository.getEventById(event1.id))
  }

  @Test
  fun deleteEvent_shouldThrowWhenIdDoNotExist() {
    runBlocking {
      repository.insertEvent(event1)

      assertThrows(IllegalArgumentException::class.java) {
        runBlocking { repository.deleteEvent(event2.id) }
      }
    }
  }

  @Test
  fun getEventsBetweenDates_shouldReturnEventsWithinRange() = runBlocking {
    repository.insertEvent(event1)
    repository.insertEvent(event2)
    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-31T23:59:59Z"))
    assertEquals(1, results.size)
    assertEquals(event1.id, results.first().id)
  }

  @Test
  fun getEventsBetweenDates_shouldThrowWhenStartAfterEnd() {
    assertThrows(IllegalArgumentException::class.java) {
      runBlocking {
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-10T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z"))
      }
    }
  }

  @Test
  fun getAllUnsyncedEvents_shouldReturnEventsNotSyncedToGivenDb() = runBlocking {
    repository.insertEvent(event1)
    repository.insertEvent(event2)
    val unsyncedToFirestore = repository.getAllUnsyncedEvents(StorageStatus.FIRESTORE)
    print(unsyncedToFirestore)
    assertTrue(unsyncedToFirestore.any { it.id == event1.id })
    assertFalse(unsyncedToFirestore.any { it.id == event2.id })
  }
}
