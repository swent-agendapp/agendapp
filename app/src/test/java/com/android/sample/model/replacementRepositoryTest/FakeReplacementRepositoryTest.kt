package com.android.sample.model.replacementRepositoryTest

import com.android.sample.data.fake.repositories.FakeReplacementRepository
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.ui.theme.EventPalette
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FakeReplacementRepository
 *
 * Verifies basic CRUD operations and filtering logic.
 */
class FakeReplacementRepositoryTest { // Later make this implement RequireSelectedOrganizationTest

  private lateinit var repository: FakeReplacementRepository
  private lateinit var sampleEvent: Event
  private val testOrgId = "ORG123"

  @Before
  fun setup() {
    repository = FakeReplacementRepository()

    val category = EventCategory(label = "Test Category", color = EventPalette.Blue)

    sampleEvent =
        Event(
            id = "E001",
            organizationId = testOrgId,
            title = "Test Event",
            description = "Sample event",
            startDate = Instant.now(),
            endDate = Instant.now().plusSeconds(3600),
            participants = setOf("Alice", "Bob"),
            recurrenceStatus = RecurrenceStatus.OneTime,
            hasBeenDeleted = false,
            category = category,
            version = 1L,
            locallyStoredBy = listOf("LOCAL"),
            cloudStorageStatuses = emptySet(),
            personalNotes = null,
            presence = emptyMap(),
            location = null
        )
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

  @Test
  fun deleteReplacement_success() = runBlocking {
    val replacement =
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)

    repository.insertReplacement(testOrgId, replacement)

    repository.deleteReplacement(testOrgId, replacement.id)

    assertNull(repository.getReplacementById(testOrgId, replacement.id))
    assertTrue(repository.getAllReplacements(testOrgId).isEmpty())
  }

  @Test
  fun getReplacementsByAbsentUser_success() = runBlocking {
    val r1 = Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    val r2 = Replacement(absentUserId = "Bob", substituteUserId = "Eve", event = sampleEvent)

    repository.insertReplacement(testOrgId, r1)
    repository.insertReplacement(testOrgId, r2)

    val result = repository.getReplacementsByAbsentUser(testOrgId, "Alice")

    assertEquals(1, result.size)
    assertEquals("Alice", result.first().absentUserId)
  }

  @Test
  fun getReplacementsBySubstituteUser_success() = runBlocking {
    val r1 = Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    val r2 = Replacement(absentUserId = "Bob", substituteUserId = "Charlie", event = sampleEvent)

    repository.insertReplacement(testOrgId, r1)
    repository.insertReplacement(testOrgId, r2)

    val result = repository.getReplacementsBySubstituteUser(testOrgId, "Charlie")

    assertEquals(2, result.size)
  }

  @Test
  fun getReplacementsByStatus_success() = runBlocking {
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

    val toProcess = repository.getReplacementsByStatus(testOrgId, ReplacementStatus.ToProcess)
    val accepted = repository.getReplacementsByStatus(testOrgId, ReplacementStatus.Accepted)

    assertEquals(1, toProcess.size)
    assertEquals(1, accepted.size)
    assertEquals("Alice", toProcess.first().absentUserId)
    assertEquals("Bob", accepted.first().absentUserId)
  }

  @Test
  fun getAllReplacementsIndependentOfOrgId() = runBlocking {
    val org1 = "ORG1"
    val org2 = "ORG2"

    val r1 = Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent)
    val r2 = Replacement(absentUserId = "Bob", substituteUserId = "Eve", event = sampleEvent)

    repository.insertReplacement(org1, r1)
    repository.insertReplacement(org2, r2)

    assertEquals(2, repository.getAllReplacements(org1).size)
    assertEquals(2, repository.getAllReplacements(org2).size)
  }

  @Test
  fun getReplacementById_notFound_returnsNull() = runBlocking {
    val result = repository.getReplacementById(testOrgId, "INVALID_ID")
    assertNull(result)
  }
}
