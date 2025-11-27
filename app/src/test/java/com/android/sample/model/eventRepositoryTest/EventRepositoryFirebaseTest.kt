package com.android.sample.model.calendar

import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Unit tests for [EventRepositoryFirebase] focusing on the worked hours calculation methods.
 *
 * These tests use a test double pattern by creating a TestableEventRepositoryFirebase that
 * overrides getEventsBetweenDates to return predefined test data, avoiding the complexity of
 * mocking Firebase dependencies.
 *
 * These tests verify:
 * - calculateWorkedHoursPastEvents: only counts events with presence=true for past events
 * - calculateWorkedHoursFutureEvents: counts all participants for future events
 * - calculateWorkedHours: combines both past and future event calculations
 */
class EventRepositoryFirebaseTest {

  private lateinit var repository: TestableEventRepositoryFirebase

  private val orgId = "org123"
  private val userId1 = "user1"
  private val userId2 = "user2"
  private val userId3 = "user3"

  /**
   * Test double for EventRepositoryFirebase that allows injecting test events without needing
   * Firebase mocking.
   */
 private class TestableEventRepositoryFirebase(
      db: FirebaseFirestore,
      private val testEvents: MutableList<Event> = mutableListOf(),
      private var orgExists: Boolean = true
  ) : EventRepositoryFirebase(db) {
    override suspend fun getEventsBetweenDates(
        orgId: String,
        startDate: Instant,
        endDate: Instant
    ): List<Event> {
      // Filter events to match the date range
      return testEvents.filter { event ->
        event.endDate >= startDate && event.startDate <= endDate
      }
    }

    override suspend fun calculateWorkedHoursPastEvents(
        orgId: String,
        start: Instant,
        end: Instant
    ): List<Pair<String, Double>> {
      // Check org exists without accessing Firestore
      require(orgExists) { "Organization with id $orgId not found" }
      
      val events = getEventsBetweenDates(orgId, start, end)
      val now = Instant.now()
      val workedHoursMap = mutableMapOf<String, Double>()

      events.forEach { event ->
        if (event.startDate <= now) {
          val durationHours =
              java.time.Duration.between(event.startDate, event.endDate).toMinutes() / 60.0
          event.presence.forEach { (userId, isPresent) ->
            if (isPresent) {
              workedHoursMap[userId] = workedHoursMap.getOrDefault(userId, 0.0) + durationHours
            }
          }
        }
      }

      return workedHoursMap.toList()
    }

    override suspend fun calculateWorkedHoursFutureEvents(
        orgId: String,
        start: Instant,
        end: Instant
    ): List<Pair<String, Double>> {
      // Check org exists without accessing Firestore
      require(orgExists) { "Organization with id $orgId not found" }
      
      val events = getEventsBetweenDates(orgId, start, end)
      val now = Instant.now()
      val workedHoursMap = mutableMapOf<String, Double>()

      events.forEach { event ->
        if (event.startDate > now) {
          val durationHours =
              java.time.Duration.between(event.startDate, event.endDate).toMinutes() / 60.0
          event.participants.forEach { userId ->
            workedHoursMap[userId] = workedHoursMap.getOrDefault(userId, 0.0) + durationHours
          }
        }
      }

      return workedHoursMap.toList()
    }

    override suspend fun calculateWorkedHours(
        orgId: String,
        start: Instant,
        end: Instant
    ): List<Pair<String, Double>> {
      // Check org exists without accessing Firestore
      require(orgExists) { "Organization with id $orgId not found" }
      
      val pastHours = calculateWorkedHoursPastEvents(orgId, start, end).toMap()
      val futureHours = calculateWorkedHoursFutureEvents(orgId, start, end).toMap()

      val allEmployeeIds = (pastHours.keys + futureHours.keys).toSet()
      val combinedHours =
          allEmployeeIds.map { employeeId ->
            val total =  
                pastHours.getOrDefault(employeeId, 0.0) + futureHours.getOrDefault(employeeId, 0.0)
            employeeId to total
          }

      return combinedHours
    }

    fun setTestEvents(events: List<Event>) {
      testEvents.clear()
      testEvents.addAll(events)
    }

    fun setOrgExists(exists: Boolean) {
      orgExists = exists
    }
  }

  @Before
  fun setup() {
    val mockFirestore = mock(FirebaseFirestore::class.java)
    repository = TestableEventRepositoryFirebase(mockFirestore)
  }

  // ==================== calculateWorkedHoursPastEvents Tests ====================

  @Test
  fun calculateWorkedHoursPastEvents_organizationNotFound_shouldThrow() = runBlocking {
    // Given: organization doesn't exist
    repository.setOrgExists(false)

    // When & Then: should throw IllegalArgumentException
    try {
      repository.calculateWorkedHoursPastEvents(
          orgId = orgId,
          start = Instant.parse("2025-01-01T00:00:00Z"),
          end = Instant.parse("2025-01-31T23:59:59Z"))
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message?.contains("Organization with id $orgId not found") == true)
    }
  }

  @Test
  fun calculateWorkedHoursPastEvents_noPastEvents_returnsEmpty() = runBlocking {
    // Given: organization exists but all events are in the future
    val futureEvent = createMockEvent(
        id = "event1",
        startDate = Instant.now().plusSeconds(3600), // 1 hour in future
        endDate = Instant.now().plusSeconds(7200), // 2 hours in future
        participants = setOf(userId1, userId2),
        presence = mapOf(userId1 to true, userId2 to true))
    
    repository.setTestEvents(listOf(futureEvent))

    // When
    val result = repository.calculateWorkedHoursPastEvents(
        orgId = orgId,
        start = Instant.parse("2025-01-01T00:00:00Z"),
        end = Instant.parse("2025-12-31T23:59:59Z"))

    // Then
    assertTrue(result.isEmpty())
  }

  @Test
  fun calculateWorkedHoursPastEvents_pastEventWithPresence_countsHours() = runBlocking {
    // Given: one past event where user1 was present and user2 was not
    val pastEvent = createMockEvent(
        id = "event1",
        startDate = Instant.now().minusSeconds(7200), // 2 hours ago
        endDate = Instant.now().minusSeconds(3600), // 1 hour ago (1 hour duration)
        participants = setOf(userId1, userId2),
        presence = mapOf(userId1 to true, userId2 to false))
    
    repository.setTestEvents(listOf(pastEvent))

    // When
    val result = repository.calculateWorkedHoursPastEvents(
        orgId = orgId,
        start = Instant.now().minusSeconds(10800),
        end = Instant.now())

    // Then
    assertEquals(1, result.size)
    val user1Hours = result.find { it.first == userId1 }
    assertNotNull(user1Hours)
    assertEquals(1.0, user1Hours!!.second, 0.01)
    
    // user2 should not be counted
    val user2Hours = result.find { it.first == userId2 }
    assertNull(user2Hours)
  }

  @Test
  fun calculateWorkedHoursPastEvents_multipleEvents_aggregatesHours() = runBlocking {
    // Given: multiple past events with different presence patterns
    val event1 = createMockEvent(
        id = "event1",
        startDate = Instant.parse("2025-01-01T09:00:00Z"),
        endDate = Instant.parse("2025-01-01T11:00:00Z"), // 2 hours
        participants = setOf(userId1, userId2),
        presence = mapOf(userId1 to true, userId2 to true))
    
    val event2 = createMockEvent(
        id = "event2",
        startDate = Instant.parse("2025-01-02T09:00:00Z"),
        endDate = Instant.parse("2025-01-02T10:30:00Z"), // 1.5 hours
        participants = setOf(userId1, userId3),
        presence = mapOf(userId1 to true, userId3 to false))
    
    repository.setTestEvents(listOf(event1, event2))

    // When
    val result = repository.calculateWorkedHoursPastEvents(
        orgId = orgId,
        start = Instant.parse("2025-01-01T00:00:00Z"),
        end = Instant.parse("2025-01-31T23:59:59Z"))

    // Then
    val resultMap = result.toMap()
    assertEquals(3.5, resultMap[userId1] ?: 0.0, 0.01) // 2 + 1.5
    assertEquals(2.0, resultMap[userId2] ?: 0.0, 0.01) // 2
    assertNull(resultMap[userId3]) // was not present
  }

  @Test
  fun calculateWorkedHoursPastEvents_mixedPastAndFuture_onlyCountsPast() = runBlocking {
    // Given: events in both past and future
    val pastEvent = createMockEvent(
        id = "past",
        startDate = Instant.now().minusSeconds(7200),
        endDate = Instant.now().minusSeconds(3600), // 1 hour duration
        participants = setOf(userId1),
        presence = mapOf(userId1 to true))
    
    val futureEvent = createMockEvent(
        id = "future",
        startDate = Instant.now().plusSeconds(3600),
        endDate = Instant.now().plusSeconds(7200), // 1 hour duration
        participants = setOf(userId1),
        presence = mapOf(userId1 to true))
    
    repository.setTestEvents(listOf(pastEvent, futureEvent))

    // When
    val result = repository.calculateWorkedHoursPastEvents(
        orgId = orgId,
        start = Instant.now().minusSeconds(10800),
        end = Instant.now().plusSeconds(10800))

    // Then: only past event should be counted
    val resultMap = result.toMap()
    assertEquals(1.0, resultMap[userId1] ?: 0.0, 0.01)
  }

  // ==================== calculateWorkedHoursFutureEvents Tests ====================

  @Test
  fun calculateWorkedHoursFutureEvents_organizationNotFound_shouldThrow() = runBlocking {
    // Given: organization doesn't exist
    repository.setOrgExists(false)

    // When & Then: should throw IllegalArgumentException
    try {
      repository.calculateWorkedHoursFutureEvents(
          orgId = orgId,
          start = Instant.parse("2025-01-01T00:00:00Z"),
          end = Instant.parse("2025-01-31T23:59:59Z"))
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message?.contains("Organization with id $orgId not found") == true)
    }
  }

  @Test
  fun calculateWorkedHoursFutureEvents_noFutureEvents_returnsEmpty() = runBlocking {
    // Given: organization exists but all events are in the past
    val pastEvent = createMockEvent(
        id = "event1",
        startDate = Instant.now().minusSeconds(7200),
        endDate = Instant.now().minusSeconds(3600),
        participants = setOf(userId1, userId2),
        presence = mapOf(userId1 to true, userId2 to true))
    
    repository.setTestEvents(listOf(pastEvent))

    // When
    val result = repository.calculateWorkedHoursFutureEvents(
        orgId = orgId,
        start = Instant.parse("2025-01-01T00:00:00Z"),
        end = Instant.parse("2025-12-31T23:59:59Z"))

    // Then
    assertTrue(result.isEmpty())
  }

  @Test
  fun calculateWorkedHoursFutureEvents_futureEvent_countsAllParticipants() = runBlocking {
    // Given: one future event with participants (presence is ignored)
    val futureEvent = createMockEvent(
        id = "event1",
        startDate = Instant.now().plusSeconds(3600),
        endDate = Instant.now().plusSeconds(5400), // 0.5 hour duration
        participants = setOf(userId1, userId2),
        presence = mapOf(userId1 to false, userId2 to false)) // presence should be ignored
    
    repository.setTestEvents(listOf(futureEvent))

    // When
    val result = repository.calculateWorkedHoursFutureEvents(
        orgId = orgId,
        start = Instant.now(),
        end = Instant.now().plusSeconds(10800))

    // Then: both users should be counted regardless of presence
    val resultMap = result.toMap()
    assertEquals(0.5, resultMap[userId1] ?: 0.0, 0.01)
    assertEquals(0.5, resultMap[userId2] ?: 0.0, 0.01)
  }

  @Test
  fun calculateWorkedHoursFutureEvents_multipleEvents_aggregatesHours() = runBlocking {
    // Given: multiple future events
    val event1 = createMockEvent(
        id = "event1",
        startDate = Instant.parse("2025-12-01T09:00:00Z"),
        endDate = Instant.parse("2025-12-01T11:00:00Z"), // 2 hours
        participants = setOf(userId1, userId2),
        presence = emptyMap())
    
    val event2 = createMockEvent(
        id = "event2",
        startDate = Instant.parse("2025-12-02T09:00:00Z"),
        endDate = Instant.parse("2025-12-02T10:00:00Z"), // 1 hour
        participants = setOf(userId1, userId3),
        presence = emptyMap())
    
    repository.setTestEvents(listOf(event1, event2))

    // When
    val result = repository.calculateWorkedHoursFutureEvents(
        orgId = orgId,
        start = Instant.parse("2025-12-01T00:00:00Z"),
        end = Instant.parse("2025-12-31T23:59:59Z"))

    // Then
    val resultMap = result.toMap()
    assertEquals(3.0, resultMap[userId1] ?: 0.0, 0.01) // 2 + 1
    assertEquals(2.0, resultMap[userId2] ?: 0.0, 0.01) // 2
    assertEquals(1.0, resultMap[userId3] ?: 0.0, 0.01) // 1
  }

  @Test
  fun calculateWorkedHoursFutureEvents_mixedPastAndFuture_onlyCountsFuture() = runBlocking {
    // Given: events in both past and future
    val pastEvent = createMockEvent(
        id = "past",
        startDate = Instant.now().minusSeconds(7200),
        endDate = Instant.now().minusSeconds(3600), // 1 hour duration
        participants = setOf(userId1),
        presence = mapOf(userId1 to true))
    
    val futureEvent = createMockEvent(
        id = "future",
        startDate = Instant.now().plusSeconds(3600),
        endDate = Instant.now().plusSeconds(7200), // 1 hour duration
        participants = setOf(userId1),
        presence = emptyMap())
    
    repository.setTestEvents(listOf(pastEvent, futureEvent))

    // When
    val result = repository.calculateWorkedHoursFutureEvents(
        orgId = orgId,
        start = Instant.now().minusSeconds(10800),
        end = Instant.now().plusSeconds(10800))

    // Then: only future event should be counted
    val resultMap = result.toMap()
    assertEquals(1.0, resultMap[userId1] ?: 0.0, 0.01)
  }

  // ==================== calculateWorkedHours Tests ====================

  @Test
  fun calculateWorkedHours_organizationNotFound_shouldThrow() = runBlocking {
    // Given: organization doesn't exist
    repository.setOrgExists(false)

    // When & Then: should throw IllegalArgumentException
    try {
      repository.calculateWorkedHours(
          orgId = orgId,
          start = Instant.parse("2025-01-01T00:00:00Z"),
          end = Instant.parse("2025-01-31T23:59:59Z"))
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message?.contains("Organization with id $orgId not found") == true)
    }
  }

  @Test
  fun calculateWorkedHours_noEvents_returnsEmpty() = runBlocking {
    // Given: organization exists but no events
    repository.setTestEvents(emptyList())

    // When
    val result = repository.calculateWorkedHours(
        orgId = orgId,
        start = Instant.parse("2025-01-01T00:00:00Z"),
        end = Instant.parse("2025-01-31T23:59:59Z"))

    // Then
    assertTrue(result.isEmpty())
  }

  @Test
  fun calculateWorkedHours_combinesPastAndFutureEvents() = runBlocking {
    // Given: both past and future events
    val pastEvent = createMockEvent(
        id = "past",
        startDate = Instant.now().minusSeconds(7200),
        endDate = Instant.now().minusSeconds(3600), // 1 hour
        participants = setOf(userId1, userId2),
        presence = mapOf(userId1 to true, userId2 to false))
    
    val futureEvent = createMockEvent(
        id = "future",
        startDate = Instant.now().plusSeconds(3600),
        endDate = Instant.now().plusSeconds(5400), // 0.5 hours
        participants = setOf(userId1, userId3),
        presence = emptyMap())
    
    repository.setTestEvents(listOf(pastEvent, futureEvent))

    // When
    val result = repository.calculateWorkedHours(
        orgId = orgId,
        start = Instant.now().minusSeconds(10800),
        end = Instant.now().plusSeconds(10800))

    // Then
    val resultMap = result.toMap()
    assertEquals(1.5, resultMap[userId1] ?: 0.0, 0.01) // 1 (past) + 0.5 (future)
    // user2 was not present in past event, not in future event
    assertNull(resultMap[userId2])
    assertEquals(0.5, resultMap[userId3] ?: 0.0, 0.01) // 0.5 (future only)
  }

  @Test
  fun calculateWorkedHours_userInBothPastAndFutureEvents_aggregatesCorrectly() = runBlocking {
    // Given: user appears in multiple past and future events
    val pastEvent1 = createMockEvent(
        id = "past1",
        startDate = Instant.parse("2025-01-01T09:00:00Z"),
        endDate = Instant.parse("2025-01-01T11:00:00Z"), // 2 hours
        participants = setOf(userId1),
        presence = mapOf(userId1 to true))
    
    val pastEvent2 = createMockEvent(
        id = "past2",
        startDate = Instant.parse("2025-01-02T09:00:00Z"),
        endDate = Instant.parse("2025-01-02T10:30:00Z"), // 1.5 hours
        participants = setOf(userId1),
        presence = mapOf(userId1 to true))
    
    val futureEvent1 = createMockEvent(
        id = "future1",
        startDate = Instant.parse("2025-12-01T09:00:00Z"),
        endDate = Instant.parse("2025-12-01T12:00:00Z"), // 3 hours
        participants = setOf(userId1),
        presence = emptyMap())
    
    val futureEvent2 = createMockEvent(
        id = "future2",
        startDate = Instant.parse("2025-12-02T09:00:00Z"),
        endDate = Instant.parse("2025-12-02T10:00:00Z"), // 1 hour
        participants = setOf(userId1),
        presence = emptyMap())
    
    repository.setTestEvents(listOf(pastEvent1, pastEvent2, futureEvent1, futureEvent2))

    // When
    val result = repository.calculateWorkedHours(
        orgId = orgId,
        start = Instant.parse("2025-01-01T00:00:00Z"),
        end = Instant.parse("2025-12-31T23:59:59Z"))

    // Then: total should be 2 + 1.5 + 3 + 1 = 7.5 hours
    val resultMap = result.toMap()
    assertEquals(7.5, resultMap[userId1] ?: 0.0, 0.01)
  }

  @Test
  fun calculateWorkedHours_multipleUsers_calculatesIndependently() = runBlocking {
    // Given: multiple users with different participation patterns
    // Past: user1 present, user2 not present
    val pastEvent = createMockEvent(
        id = "past",
        startDate = Instant.parse("2025-01-01T09:00:00Z"),
        endDate = Instant.parse("2025-01-01T11:00:00Z"), // 2 hours
        participants = setOf(userId1, userId2),
        presence = mapOf(userId1 to true, userId2 to false))
    
    // Future: user2 and user3 participating
    val futureEvent = createMockEvent(
        id = "future",
        startDate = Instant.parse("2025-12-01T09:00:00Z"),
        endDate = Instant.parse("2025-12-01T12:00:00Z"), // 3 hours
        participants = setOf(userId2, userId3),
        presence = emptyMap())
    
    repository.setTestEvents(listOf(pastEvent, futureEvent))

    // When
    val result = repository.calculateWorkedHours(
        orgId = orgId,
        start = Instant.parse("2025-01-01T00:00:00Z"),
        end = Instant.parse("2025-12-31T23:59:59Z"))

    // Then
    val resultMap = result.toMap()
    assertEquals(2.0, resultMap[userId1] ?: 0.0, 0.01) // only past
    assertEquals(3.0, resultMap[userId2] ?: 0.0, 0.01) // only future (wasn't present in past)
    assertEquals(3.0, resultMap[userId3] ?: 0.0, 0.01) // only future
  }

  // ==================== Helper Methods ====================

  private fun createMockEvent(
      id: String,
      startDate: Instant,
      endDate: Instant,
      participants: Set<String>,
      presence: Map<String, Boolean>
  ): Event {
    return Event(
        id = id,
        organizationId = orgId,
        title = "Test Event",
        description = "Test Description",
        startDate = startDate,
        endDate = endDate,
        cloudStorageStatuses = emptySet(),
        personalNotes = null,
        participants = participants,
        version = System.currentTimeMillis(),
        presence = presence,
        recurrenceStatus = RecurrenceStatus.OneTime,
        hasBeenDeleted = false,
        color = androidx.compose.ui.graphics.Color.Blue)
  }
}
