package com.android.sample.model.replacementRepositoryTest

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
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
  private val testOrgId = "ORG123"

  @Before
  fun setup() {
    repository = ReplacementRepositoryLocal()

    val eventCategory =
        EventCategory(
            organizationId = testOrgId,
            label = "Test Category",
            color = EventPalette.Blue,
        )

    sampleEvent =
        Event(
            id = "E001",
            organizationId = "ORG123",
            title = "Weekly Sync",
            description = "Team meeting",
            startDate = Instant.now(),
            endDate = Instant.now().plusSeconds(3600),
            participants = setOf("Alice", "Bob"),
            recurrenceStatus = RecurrenceStatus.OneTime,
            hasBeenDeleted = false,
            category = eventCategory,
            version = System.currentTimeMillis(),
            locallyStoredBy = listOf("LOCAL_USER"),
            cloudStorageStatuses = emptySet(),
            personalNotes = null)
  }

  @Test
  fun insertAndRetrieveReplacement_success() = runBlocking {
    val replacement =
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)

    repository.insertReplacement(testOrgId, replacement)

    val retrieved = repository.getReplacementById(testOrgId, replacement.id)
    assertNotNull(retrieved)
    assertEquals("Alice", retrieved?.absentUserId)
    assertEquals("Charlie", retrieved?.substituteUserId)
    assertEquals(ReplacementStatus.ToProcess, retrieved?.status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertDuplicateReplacement_fails() = runBlocking {
    val replacement =
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    repository.insertReplacement(testOrgId, replacement)
    // Second insertion with same ID should fail
    repository.insertReplacement(testOrgId, replacement)
  }

  @Test
  fun updateReplacement_success() = runBlocking {
    val replacement =
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    repository.insertReplacement(testOrgId, replacement)

    val updated = replacement.copy(status = ReplacementStatus.Accepted)
    repository.updateReplacement(testOrgId, replacement.id, updated)

    val retrieved = repository.getReplacementById(testOrgId, replacement.id)
    assertEquals(ReplacementStatus.Accepted, retrieved?.status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateNonexistentReplacement_fails() = runBlocking {
    val nonExistentId = UUID.randomUUID().toString()
    repository.updateReplacement(
        testOrgId,
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
    repository.insertReplacement(testOrgId, replacement)
    repository.deleteReplacement(testOrgId, replacement.id)

    assertNull(repository.getReplacementById(testOrgId, replacement.id))
    assertTrue(repository.getAllReplacements(testOrgId).isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteNonexistentReplacement_fails() = runBlocking {
    repository.deleteReplacement(testOrgId, UUID.randomUUID().toString())
  }

  @Test
  fun getReplacementsByAbsentUser_returnsCorrectResults() = runBlocking {
    val r1 = Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    val r2 = Replacement(absentUserId = "Bob", substituteUserId = "Eve", event = sampleEvent)
    repository.insertReplacement(testOrgId, r1)
    repository.insertReplacement(testOrgId, r2)

    val result = repository.getReplacementsByAbsentUser(testOrgId, "Alice")
    assertEquals(1, result.size)
    assertEquals("Alice", result.first().absentUserId)
  }

  @Test
  fun getReplacementsBySubstituteUser_returnsCorrectResults() = runBlocking {
    val r1 = Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    val r2 = Replacement(absentUserId = "Bob", substituteUserId = "Charlie", event = sampleEvent)
    repository.insertReplacement(testOrgId, r1)
    repository.insertReplacement(testOrgId, r2)

    val result = repository.getReplacementsBySubstituteUser(testOrgId, "Charlie")
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
    repository.insertReplacement(testOrgId, r1)
    repository.insertReplacement(testOrgId, r2)

    val pending = repository.getReplacementsByStatus(testOrgId, ReplacementStatus.ToProcess)
    val accepted = repository.getReplacementsByStatus(testOrgId, ReplacementStatus.Accepted)

    assertEquals(1, pending.size)
    assertEquals(1, accepted.size)
    assertEquals("Alice", pending.first().absentUserId)
    assertEquals("Bob", accepted.first().absentUserId)
  }

  @Test
  fun getAllReplacements_forDifferentOrganizations() = runBlocking {
    val orgId1 = "ORG1"
    val orgId2 = "ORG2"

    val r1 = Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    val r2 = Replacement(absentUserId = "Bob", substituteUserId = "Eve", event = sampleEvent)

    repository.insertReplacement(orgId1, r1)
    repository.insertReplacement(orgId2, r2)

    val org1Replacements = repository.getAllReplacements(orgId1)
    val org2Replacements = repository.getAllReplacements(orgId2)

    assertEquals(1, org1Replacements.size)
    assertEquals("Alice", org1Replacements.first().absentUserId)

    assertEquals(1, org2Replacements.size)
    assertEquals("Bob", org2Replacements.first().absentUserId)
  }

  @Test
  fun getNoReplacementsForInvalidOrganization() = runBlocking {
    // Insert a replacement for a valid organization
    repository.insertReplacement(
        testOrgId,
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent))

    val invalidOrgId = "INVALID_ORG"

    // Attempt to retrieve replacements for a non-existent organization
    val replacements = repository.getAllReplacements(invalidOrgId)
    assertTrue(replacements.isEmpty())
  }
}
