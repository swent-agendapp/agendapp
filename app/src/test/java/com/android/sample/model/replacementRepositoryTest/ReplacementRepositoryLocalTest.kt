package com.android.sample.model.replacementRepositoryTest

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementRepositoryLocal
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.ui.theme.EventPalette
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// Assisted by AI

/**
 * Unit tests for [ReplacementRepositoryLocal].
 *
 * Verifies CRUD operations and filtering behaviors.
 */
class ReplacementRepositoryLocalTest {

  private lateinit var repository: ReplacementRepositoryLocal
  private lateinit var sampleEvent: Event

  @Before
  fun setup() {
    repository = ReplacementRepositoryLocal()
    sampleEvent =
        Event(
            id = "E001",
            title = "Weekly Sync",
            description = "Team meeting",
            startDate = Instant.now(),
            endDate = Instant.now().plusSeconds(3600),
            participants = setOf("Alice", "Bob"),
            recurrenceStatus = RecurrenceStatus.OneTime,
            hasBeenDeleted = false,
            color = EventPalette.Blue,
            version = System.currentTimeMillis(),
            locallyStoredBy = listOf("LOCAL_USER"),
            cloudStorageStatuses = emptySet(),
            personalNotes = null)
  }

  @Test
  fun insertAndRetrieveReplacement_success() = runBlocking {
    val replacement =
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)

    repository.insertReplacement(replacement)

    val retrieved = repository.getReplacementById(replacement.id)
    assertNotNull(retrieved)
    assertEquals("Alice", retrieved?.absentUserId)
    assertEquals("Charlie", retrieved?.substituteUserId)
    assertEquals(ReplacementStatus.ToProcess, retrieved?.status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertDuplicateReplacement_fails() = runBlocking {
    val replacement =
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    repository.insertReplacement(replacement)
    // Second insertion with same ID should fail
    repository.insertReplacement(replacement)
  }

  @Test
  fun updateReplacement_success() = runBlocking {
    val replacement =
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    repository.insertReplacement(replacement)

    val updated = replacement.copy(status = ReplacementStatus.Accepted)
    repository.updateReplacement(replacement.id, updated)

    val retrieved = repository.getReplacementById(replacement.id)
    assertEquals(ReplacementStatus.Accepted, retrieved?.status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateNonexistentReplacement_fails() = runBlocking {
    val nonExistentId = UUID.randomUUID().toString()
    repository.updateReplacement(
        nonExistentId,
        Replacement(
            id = nonExistentId,
            absentUserId = "Alice",
            substituteUserId = "Bob",
            event = sampleEvent))
  }

  @Test
  fun deleteReplacement_success() = runBlocking {
    val replacement =
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    repository.insertReplacement(replacement)
    repository.deleteReplacement(replacement.id)

    assertNull(repository.getReplacementById(replacement.id))
    assertTrue(repository.getAllReplacements().isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteNonexistentReplacement_fails() = runBlocking {
    repository.deleteReplacement(UUID.randomUUID().toString())
  }

  @Test
  fun getReplacementsByAbsentUser_returnsCorrectResults() = runBlocking {
    val r1 = Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    val r2 = Replacement(absentUserId = "Bob", substituteUserId = "Eve", event = sampleEvent)
    repository.insertReplacement(r1)
    repository.insertReplacement(r2)

    val result = repository.getReplacementsByAbsentUser("Alice")
    assertEquals(1, result.size)
    assertEquals("Alice", result.first().absentUserId)
  }

  @Test
  fun getReplacementsBySubstituteUser_returnsCorrectResults() = runBlocking {
    val r1 = Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    val r2 = Replacement(absentUserId = "Bob", substituteUserId = "Charlie", event = sampleEvent)
    repository.insertReplacement(r1)
    repository.insertReplacement(r2)

    val result = repository.getReplacementsBySubstituteUser("Charlie")
    assertEquals(2, result.size)
  }

  @Test
  fun getReplacementsByStatus_returnsCorrectResults() = runBlocking {
    val r1 =
        Replacement(
            absentUserId = "Alice",
            substituteUserId = "Charlie",
            event = sampleEvent,
            status = ReplacementStatus.ToProcess)
    val r2 =
        Replacement(
            absentUserId = "Bob",
            substituteUserId = "Eve",
            event = sampleEvent,
            status = ReplacementStatus.Accepted)
    repository.insertReplacement(r1)
    repository.insertReplacement(r2)

    val pending = repository.getReplacementsByStatus(ReplacementStatus.ToProcess)
    val accepted = repository.getReplacementsByStatus(ReplacementStatus.Accepted)

    assertEquals(1, pending.size)
    assertEquals(1, accepted.size)
    assertEquals("Alice", pending.first().absentUserId)
    assertEquals("Bob", accepted.first().absentUserId)
  }
}
