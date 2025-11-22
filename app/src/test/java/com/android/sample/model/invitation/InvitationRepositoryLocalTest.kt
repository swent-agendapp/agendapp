package com.android.sample.model.invitation

import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationRepositoryLocal
import com.android.sample.model.organization.invitation.InvitationStatus
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// Assisted by AI

/**
 * Unit tests for [InvitationRepositoryLocal].
 *
 * Verifies CRUD operations and repository behaviors.
 */
class InvitationRepositoryLocalTest {

  private lateinit var repository: InvitationRepositoryLocal

  @Before
  fun setup() {
    repository = InvitationRepositoryLocal()
  }

  @Test
  fun insertAndRetrieveInvitation_success() = runBlocking {
    val invitation = Invitation.create(organizationId = "ORG123")

    repository.insertInvitation(invitation)

    val retrieved = repository.getInvitationById(invitation.id)
    assertNotNull(retrieved)
    assertEquals("ORG123", retrieved?.organizationId)
    assertEquals(invitation.code, retrieved?.code)
    assertEquals(InvitationStatus.Active, retrieved?.status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertDuplicateInvitation_fails() = runBlocking {
    val invitation = Invitation.create("ORG123")
    repository.insertInvitation(invitation)

    // Same ID → must fail
    repository.insertInvitation(invitation)
  }

  @Test
  fun updateInvitation_success() = runBlocking {
    val invitation = Invitation.create("ORG123")
    repository.insertInvitation(invitation)

    val updated = invitation.copy(acceptedAt = Instant.now(), status = InvitationStatus.Used)
    repository.updateInvitation(invitation.id, updated)

    val retrieved = repository.getInvitationById(invitation.id)
    assertEquals(InvitationStatus.Used, retrieved?.status)
    assertNotNull(retrieved?.acceptedAt)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateNonexistentInvitation_fails() = runBlocking {
    val randomId = UUID.randomUUID().toString()

    repository.updateInvitation(
        randomId,
        Invitation(
            id = randomId,
            organizationId = "ORG999",
            code = "ABC123",
            status = InvitationStatus.Active))
  }

  @Test
  fun deleteInvitation_success() = runBlocking {
    val invitation = Invitation.create("ORG123")
    repository.insertInvitation(invitation)

    repository.deleteInvitation(invitation.id)

    assertNull(repository.getInvitationById(invitation.id))
    assertTrue(repository.getAllInvitations().isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteNonexistentInvitation_fails() = runBlocking {
    repository.deleteInvitation(UUID.randomUUID().toString())
  }

  @Test
  fun getInvitationsByOrganization_returnsCorrectResults() = runBlocking {
    val i1 = Invitation.create("ORG123")
    val i2 = Invitation.create("ORG456")
    val i3 = Invitation.create("ORG123")

    repository.insertInvitation(i1)
    repository.insertInvitation(i2)
    repository.insertInvitation(i3)

    val result = repository.getAllInvitations().filter { it.organizationId == "ORG123" }

    assertEquals(2, result.size)
    assertTrue(result.all { it.organizationId == "ORG123" })
  }

  @Test
  fun getInvitationsByStatus_returnsCorrectResults() = runBlocking {
    val active = Invitation.create("ORG100")
    val expired =
        Invitation(
            id = UUID.randomUUID().toString(),
            organizationId = "ORG100",
            code = "EX1234",
            status = InvitationStatus.Expired)

    repository.insertInvitation(active)
    repository.insertInvitation(expired)

    val actives = repository.getAllInvitations().filter { it.status == InvitationStatus.Active }
    val expireds = repository.getAllInvitations().filter { it.status == InvitationStatus.Expired }

    assertEquals(1, actives.size)
    assertEquals(1, expireds.size)
  }

  @Test
  fun preloadSampleData_success() = runBlocking {
    val repoWithSamples = InvitationRepositoryLocal(preloadSampleData = true)

    val all = repoWithSamples.getAllInvitations()

    assertEquals(2, all.size)
    assertNotNull(all.find { it.id == "I001" })
    assertNotNull(all.find { it.id == "I002" })
  }
}
