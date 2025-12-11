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

  private lateinit var repo: InvitationRepositoryLocal
  private lateinit var admin: User
  private lateinit var member: User
  private lateinit var outsider: User
  private lateinit var organizationA: Organization
  private lateinit var organizationB: Organization

  @Before
  fun setup() {
    repo = InvitationRepositoryLocal()

    // --- Create users ---
    admin = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
    member = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")
    outsider = User(id = "outsider", displayName = "Outsider", email = "outsider@example.com")

    // --- Create organization ---
    organizationA =
        Organization(
            id = "orgA", name = "Org A", admins = listOf(admin), members = listOf(member, admin))
    organizationB =
        Organization(id = "orgB", name = "Org B", admins = listOf(admin), members = listOf(admin))
  }

  @Test
  fun insertAndRetrieveInvitation_success() = runBlocking {
    repo.insertInvitation(organizationA, admin)

    val retrievedList = repo.getAllInvitations()
    assertNotNull(retrievedList)
    assertEquals(1, retrievedList.size)
    assertEquals(organizationA.id, retrievedList.first().organizationId)
    assertEquals(InvitationStatus.Active, retrievedList.first().status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertInvitationByNonAdmin_fails() = runBlocking {
    repo.insertInvitation(organizationA, member)
  }

  @Test
  fun updateInvitation_success() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()

    val updated = retrievedInv.copy(acceptedAt = Instant.now(), status = InvitationStatus.Used)
    repo.updateInvitation(retrievedInv.id, updated, organizationA, admin)

    val retrievedUpdated = repo.getInvitationById(retrievedInv.id)
    assertEquals(InvitationStatus.Used, retrievedUpdated?.status)
    assertNotNull(retrievedUpdated?.acceptedAt)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateNonexistentInvitation_fails() = runBlocking {
    val randomId = UUID.randomUUID().toString()

    repo.updateInvitation(
        randomId,
        Invitation(
            id = randomId,
            organizationId = organizationA.id,
            code = "ABC123",
            status = InvitationStatus.Active),
        organizationA,
        admin)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateInvitationToActiveByMember_fails() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()
    val updated = retrievedInv.copy(status = InvitationStatus.Active)

    repo.updateInvitation(retrievedInv.id, updated, organizationA, member)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateInvitationToActiveByOutsider_fails() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()

    val updated = retrievedInv.copy(status = InvitationStatus.Active)

    repo.updateInvitation(retrievedInv.id, updated, organizationA, outsider)
  }

  @Test
  fun deleteInvitation_success() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()

    repo.deleteInvitation(retrievedInv.id, organizationA, admin)

    assertNull(repo.getInvitationById(retrievedInv.id))
    assertTrue(repo.getAllInvitations().isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteInvitationByNonAdmin_fails() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()

    repo.deleteInvitation(retrievedInv.id, organizationA, member)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteNonexistentInvitation_fails() = runBlocking {
    repo.deleteInvitation(UUID.randomUUID().toString(), organizationA, admin)
  }

  @Test
  fun getInvitationByOrganization_correctly_retrieves() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    repo.insertInvitation(organizationB, admin)
    repo.insertInvitation(organizationB, admin)

    val invitationsOfOrgB = repo.getInvitationByOrganization(organizationB.id)

    assertEquals(2, invitationsOfOrgB.size)
  }
}
