package com.android.sample.model.invitation

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
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
  private lateinit var admin: User
  private lateinit var member: User
  private lateinit var outsider: User
  private lateinit var orgA: Organization
  private lateinit var invitation: Invitation

  @Before
  fun setup() {
    repository = InvitationRepositoryLocal()

    // --- Create users ---
    admin = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
    member = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")
    outsider = User(id = "outsider", displayName = "Outsider", email = "outsider@example.com")

    // --- Create organization ---
    orgA =
        Organization(
            id = "orgA", name = "Org A", admins = listOf(admin), members = listOf(member, admin))

    invitation = Invitation.create(orgA)
  }

  @Test
  fun insertAndRetrieveInvitation_success() = runBlocking {
    repository.insertInvitation(invitation, admin)

    val retrieved = repository.getInvitationById(invitation.id)
    assertNotNull(retrieved)
    assertEquals(orgA, retrieved?.organization)
    assertEquals(invitation.code, retrieved?.code)
    assertEquals(InvitationStatus.Active, retrieved?.status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertDuplicateInvitation_fails() = runBlocking {
    repository.insertInvitation(invitation, admin)

    // Same ID → must fail
    repository.insertInvitation(invitation, admin)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertInvitationByNonAdmin_fails() = runBlocking {
    repository.insertInvitation(invitation, member)
  }

  @Test
  fun updateInvitation_success() = runBlocking {
    repository.insertInvitation(invitation, admin)

    val updated = invitation.copy(acceptedAt = Instant.now(), status = InvitationStatus.Used)
    repository.updateInvitation(invitation.id, updated, admin)

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
            id = randomId, organization = orgA, code = "ABC123", status = InvitationStatus.Active),
        admin)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateInvitationToActiveByMember_fails() = runBlocking {
    repository.insertInvitation(invitation, admin)
    val updated = invitation.copy(status = InvitationStatus.Active)

    repository.updateInvitation(invitation.id, updated, member)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateInvitationToActiveByOutsider_fails() = runBlocking {
    repository.insertInvitation(invitation, admin)
    val updated = invitation.copy(status = InvitationStatus.Active)

    repository.updateInvitation(invitation.id, updated, outsider)
  }

  @Test
  fun deleteInvitation_success() = runBlocking {
    repository.insertInvitation(invitation, admin)
    repository.deleteInvitation(invitation.id, admin)

    assertNull(repository.getInvitationById(invitation.id))
    assertTrue(repository.getAllInvitations().isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteInvitationByNonAdmin_fails() = runBlocking {
    repository.insertInvitation(invitation, admin)
    repository.deleteInvitation(invitation.id, member)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteNonexistentInvitation_fails() = runBlocking {
    repository.deleteInvitation(UUID.randomUUID().toString(), admin)
  }
}
