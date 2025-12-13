package com.android.sample.model.replacement

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.calendar.Event
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

// Assisted by AI

@RunWith(AndroidJUnit4::class)
class ReplacementRepositoryFirebaseTest :
    FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  private lateinit var repository: ReplacementRepository
  private lateinit var sampleEvent: Event
  private lateinit var replacement1: Replacement
  private lateinit var replacement2: Replacement

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  override fun setUp() {
    super.setUp()

    setSelectedOrganization()

    repository = createInitializedReplacementRepository()

    sampleEvent =
        createEvent(
            organizationId = organizationId,
            title = "Meeting",
            description = "Team meeting",
            startDate = Instant.parse("2025-11-12T10:00:00Z"),
            endDate = Instant.parse("2025-11-12T11:00:00Z"))[0]

    replacement1 =
        Replacement(
            id = "replacement1",
            absentUserId = "user123",
            substituteUserId = "user456",
            event = sampleEvent,
            status = ReplacementStatus.ToProcess)

    replacement2 =
        Replacement(
            id = "replacement2",
            absentUserId = "user789",
            substituteUserId = "user321",
            event = sampleEvent,
            status = ReplacementStatus.Accepted)
  }

  @Test
  fun insertReplacement_andGetById_shouldWork() = runBlocking {
    repository.insertReplacement(organizationId, replacement1)
    val retrieved = repository.getReplacementById(organizationId, replacement1.id)
    Assert.assertNotNull(retrieved)
    Assert.assertEquals(replacement1.absentUserId, retrieved?.absentUserId)
    Assert.assertEquals(replacement1.event.id, retrieved?.event?.id)
  }

  @Test
  fun getAllReplacements_shouldReturnInsertedOnes() = runBlocking {
    repository.insertReplacement(organizationId, replacement1)
    repository.insertReplacement(organizationId, replacement2)
    val allReplacements = repository.getAllReplacements(organizationId)
    Assert.assertEquals(2, allReplacements.size)
  }

  @Test
  fun updateReplacement_shouldReplaceExistingReplacement() = runBlocking {
    repository.insertReplacement(organizationId, replacement1)
    val updated = replacement1.copy(status = ReplacementStatus.Accepted)
    repository.updateReplacement(organizationId, replacement1.id, updated)
    val retrieved = repository.getReplacementById(organizationId, replacement1.id)
    Assert.assertEquals(ReplacementStatus.Accepted, retrieved?.status)
  }

  @Test
  fun deleteReplacement_shouldRemoveReplacement() = runBlocking {
    repository.insertReplacement(organizationId, replacement1)
    repository.deleteReplacement(organizationId, replacement1.id)
    Assert.assertNull(repository.getReplacementById(organizationId, replacement1.id))
  }

  @Test
  fun getReplacementsByUser_returnsCorrectly() = runBlocking {
    repository.insertReplacement(organizationId, replacement1)
    repository.insertReplacement(organizationId, replacement2)

    val user123Replacements = repository.getReplacementsByAbsentUser(organizationId, "user123")
    Assert.assertEquals(1, user123Replacements.size)
    Assert.assertEquals("replacement1", user123Replacements.first().id)
  }

  @Test
  fun getReplacementById_unknownId_returnsNull() = runBlocking {
    val retrieved = repository.getReplacementById(organizationId, "unknown-id")
    Assert.assertNull(retrieved)
  }

  @Test
  fun getReplacementsByAbsentUser_noMatch_returnsEmptyList() = runBlocking {
    repository.insertReplacement(organizationId, replacement1)
    val result = repository.getReplacementsByAbsentUser(organizationId, "someone-else")
    Assert.assertTrue(result.isEmpty())
  }

  @Test
  fun getAllReplacements_forDifferentOrganizations() = runBlocking {
    val orgId1 = "org1"
    val orgId2 = "org2"

    val replacementOrg1 =
        Replacement(
            id = "replacementOrg1",
            absentUserId = "userA",
            substituteUserId = "userB",
            event = sampleEvent,
            status = ReplacementStatus.ToProcess)

    val replacementOrg2 =
        Replacement(
            id = "replacementOrg2",
            absentUserId = "userC",
            substituteUserId = "userD",
            event = sampleEvent,
            status = ReplacementStatus.Accepted)

    repository.insertReplacement(orgId1, replacementOrg1)
    repository.insertReplacement(orgId2, replacementOrg2)

    val replacementsOrg1 = repository.getAllReplacements(orgId1)
    val replacementsOrg2 = repository.getAllReplacements(orgId2)

    Assert.assertEquals(1, replacementsOrg1.size)
    Assert.assertEquals("replacementOrg1", replacementsOrg1.first().id)

    Assert.assertEquals(1, replacementsOrg2.size)
    Assert.assertEquals("replacementOrg2", replacementsOrg2.first().id)
  }

  @Test
  fun getNoReplacementsForInvalidOrganization() = runBlocking {
    repository.insertReplacement(
        organizationId,
        Replacement(absentUserId = "Alice", substituteUserId = "Charlie", event = sampleEvent))

    val invalidOrgId = "invalidOrg"
    val replacements = repository.getAllReplacements(invalidOrgId)
    Assert.assertTrue(replacements.isEmpty())
  }
}
