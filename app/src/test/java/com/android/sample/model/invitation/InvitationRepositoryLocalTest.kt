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
  }

  @Test
  fun insertAndRetrieveInvitation_success() = runBlocking {
    repository.insertInvitation(orgA, admin)

    val retrievedList = repository.getAllInvitations()
    assertNotNull(retrievedList)
    assertEquals(1, retrievedList.size)
    assertEquals(orgA.id, retrievedList.first().organizationId)
    assertEquals(InvitationStatus.Active, retrievedList.first().status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertInvitationByNonAdmin_fails() = runBlocking { repository.insertInvitation(orgA, member) }

  @Test
  fun updateInvitation_success() = runBlocking {
    repository.insertInvitation(orgA, admin)
    val retrievedInv = repository.getAllInvitations().first()

    val updated = retrievedInv.copy(acceptedAt = Instant.now(), status = InvitationStatus.Used)
    repository.updateInvitation(retrievedInv.id, updated, orgA, admin)

    val retrievedUpdated = repository.getInvitationById(retrievedInv.id)
    assertEquals(InvitationStatus.Used, retrievedUpdated?.status)
    assertNotNull(retrievedUpdated?.acceptedAt)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateNonexistentInvitation_fails() = runBlocking {
    val randomId = UUID.randomUUID().toString()

    repository.updateInvitation(
        randomId,
        Invitation(
            id = randomId,
            organizationId = orgA.id,
            code = "ABC123",
            status = InvitationStatus.Active),
        orgA,
        admin)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateInvitationToActiveByMember_fails() = runBlocking {
    repository.insertInvitation(orgA, admin)
    val retrievedInv = repository.getAllInvitations().first()
    val updated = retrievedInv.copy(status = InvitationStatus.Active)

    repository.updateInvitation(retrievedInv.id, updated, orgA, member)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateInvitationToActiveByOutsider_fails() = runBlocking {
    repository.insertInvitation(orgA, admin)
    val retrievedInv = repository.getAllInvitations().first()

    val updated = retrievedInv.copy(status = InvitationStatus.Active)

    repository.updateInvitation(retrievedInv.id, updated, orgA, outsider)
  }

  @Test
  fun deleteInvitation_success() = runBlocking {
    repository.insertInvitation(orgA, admin)
    val retrievedInv = repository.getAllInvitations().first()

    repository.deleteInvitation(retrievedInv.id, orgA, admin)

    assertNull(repository.getInvitationById(retrievedInv.id))
    assertTrue(repository.getAllInvitations().isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteInvitationByNonAdmin_fails() = runBlocking {
    repository.insertInvitation(orgA, admin)
    val retrievedInv = repository.getAllInvitations().first()

    repository.deleteInvitation(retrievedInv.id, orgA, member)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteNonexistentInvitation_fails() = runBlocking {
    repository.deleteInvitation(UUID.randomUUID().toString(), orgA, admin)
  }
}
