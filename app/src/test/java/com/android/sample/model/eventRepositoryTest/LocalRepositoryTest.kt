package com.android.sample.model.calendar

import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [EventRepository].
 *
 * These tests verify expected repository contract behaviors using an in-memory fake implementation.
 */
class EventRepositoryTest {

  private lateinit var repository: EventRepositoryLocal
  private lateinit var sampleEvent: Event

  @Before
  fun setup() {
    repository = EventRepositoryLocal()
    sampleEvent =
        createEvent(
            title = "Meeting",
            description = "Discuss project updates",
            startDate = Instant.parse("2024-01-01T10:00:00Z"),
            endDate = Instant.parse("2024-01-01T11:00:00Z"),
            cloudStorageStatuses = emptySet(),
            participants = setOf("userB"))
  }

  @Test
  fun insertEvent_shouldAddEvent() = runBlocking {
    repository.insertEvent(sampleEvent)
    val events = repository.getAllEvents()
    assertEquals(1, events.size)
    assertEquals(sampleEvent.id, events.first().id)
  }

  @Test
  fun getEventById_shouldReturnCorrectEvent() = runBlocking {
    repository.insertEvent(sampleEvent)
    val fetched = repository.getEventById(sampleEvent.id)
    assertNotNull(fetched)
    assertEquals(sampleEvent.title, fetched?.title)
  }

  @Test
  fun updateEvent_shouldModifyExistingEvent() = runBlocking {
    repository.insertEvent(sampleEvent)
    val updated = sampleEvent.copy(title = "Updated Meeting")
    repository.updateEvent(sampleEvent.id, updated)

    val fetched = repository.getEventById(sampleEvent.id)
    assertEquals("Updated Meeting", fetched?.title)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteEvent_nonExistingId_shouldThrow() = runBlocking {
    repository.deleteEvent("nonexistent-id")
  }

  @Test
  fun deleteEvent_existingId_shouldRemoveEvent() = runBlocking {
    repository.insertEvent(sampleEvent)
    repository.deleteEvent(sampleEvent.id)

    val all = repository.getAllEvents()
    assertTrue(all.isEmpty())
  }

  @Test
  fun getEventsBetweenDates_shouldReturnEventsWithinRange() = runBlocking {
    val event1 = sampleEvent
    val event2 =
        sampleEvent.copy(
            id = "2",
            startDate = Instant.parse("2025-02-01T10:00:00Z"),
            endDate = Instant.parse("2025-02-01T11:00:00Z"),
            locallyStoredBy = emptyList())
    repository.insertEvent(event1)
    repository.insertEvent(event2)

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-01-15T00:00:00Z"), Instant.parse("2025-03-01T00:00:00Z"))
    assertEquals(event2.id, results[0].id)
  }
}
