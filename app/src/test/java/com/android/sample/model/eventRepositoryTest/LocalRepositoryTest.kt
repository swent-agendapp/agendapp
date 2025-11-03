package com.android.sample.model.calendar

import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [EventRepositoryLocal].
 *
 * These tests verify expected repository contract behaviors using an in-memory fake implementation.
 */
class LocalRepositoryTest {

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
  fun updateEvent_deletedEvent_shouldThrow() = runBlocking {
    repository.insertEvent(sampleEvent)
    val deleted = sampleEvent.copy(hasBeenDeleted = true)

    repository.updateEvent(sampleEvent.id, deleted)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertEvent_withDuplicateId_shouldThrow() = runBlocking {
    repository.insertEvent(sampleEvent)
    repository.insertEvent(sampleEvent)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteEvent_twice_shouldThrowOnSecondCall() = runBlocking {
    repository.insertEvent(sampleEvent)
    repository.deleteEvent(sampleEvent.id)

    repository.deleteEvent(sampleEvent.id)
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
  fun getEventsBetweenDates_returnsEventsFullyInsideRange() = runBlocking {
    // event fully inside the range should be returned
    repository.insertEvent(
        sampleEvent.copy(
            id = "in-range",
            startDate = Instant.parse("2025-02-10T10:00:00Z"),
            endDate = Instant.parse("2025-02-10T11:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"))

    assertEquals(1, results.size)
    assertEquals("in-range", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_excludesEventsBeforeRange() = runBlocking {
    // event ending before the start of the range should be ignored
    repository.insertEvent(
        sampleEvent.copy(
            id = "before-range",
            startDate = Instant.parse("2025-01-31T09:00:00Z"),
            endDate = Instant.parse("2025-01-31T10:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"))

    assertTrue(results.isEmpty())
  }

  @Test
  fun getEventsBetweenDates_excludesEventsAfterRange() = runBlocking {
    // event starting after the end of the range should be ignored
    repository.insertEvent(
        sampleEvent.copy(
            id = "after-range",
            startDate = Instant.parse("2025-03-01T09:00:00Z"),
            endDate = Instant.parse("2025-03-01T10:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"))

    assertTrue(results.isEmpty())
  }

  @Test
  fun getEventsBetweenDates_includesEventsOverlappingStart() = runBlocking {
    // event starting before but ending inside the range should be returned
    repository.insertEvent(
        sampleEvent.copy(
            id = "overlap-start",
            startDate = Instant.parse("2025-01-31T23:00:00Z"),
            endDate = Instant.parse("2025-02-01T01:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"))

    assertEquals(1, results.size)
    assertEquals("overlap-start", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_includesEventsOverlappingEnd() = runBlocking {
    // event starting inside but ending after the range should be returned
    repository.insertEvent(
        sampleEvent.copy(
            id = "overlap-end",
            startDate = Instant.parse("2025-02-28T22:00:00Z"),
            endDate = Instant.parse("2025-03-01T01:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"))

    assertEquals(1, results.size)
    assertEquals("overlap-end", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_includesEventsEndingExactlyAtStart() = runBlocking {
    // event ending exactly at the start of the range should be returned
    repository.insertEvent(
        sampleEvent.copy(
            id = "end-at-start",
            startDate = Instant.parse("2025-01-31T22:00:00Z"),
            endDate = Instant.parse("2025-02-01T00:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"))

    assertEquals(1, results.size)
    assertEquals("end-at-start", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_includesEventsEndingExactlyAtEnd() = runBlocking {
    // event ending exactly at the end of the range should be returned
    repository.insertEvent(
        sampleEvent.copy(
            id = "end-at-end",
            startDate = Instant.parse("2025-02-28T22:00:00Z"),
            endDate = Instant.parse("2025-02-28T23:59:59Z")))

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"))

    assertEquals(1, results.size)
    assertEquals("end-at-end", results.first().id)
  }

  @Test
  fun getEventsBetweenDates_includesEventsCoveringWholeRange() = runBlocking {
    // event that starts before and ends after the range should be returned
    repository.insertEvent(
        sampleEvent.copy(
            id = "covering",
            startDate = Instant.parse("2025-01-01T00:00:00Z"),
            endDate = Instant.parse("2025-03-01T00:00:00Z")))

    val results =
        repository.getEventsBetweenDates(
            Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"))

    assertEquals(1, results.size)
    assertEquals("covering", results.first().id)
  }

  @Test(expected = IllegalArgumentException::class)
  fun getEventsBetweenDates_invalidRange_shouldThrow() {
    runBlocking {
      repository.getEventsBetweenDates(
          Instant.parse("2025-04-01T00:00:00Z"), Instant.parse("2025-03-01T00:00:00Z"))
    }
  }
}
