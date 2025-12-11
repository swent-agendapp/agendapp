package com.android.sample.model.invitation

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.authentication.UsersRepositoryLocal
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
  private lateinit var userRepository: UserRepository
  private lateinit var admin: User
  private lateinit var member: User
  private lateinit var outsider: User
  private lateinit var organizationA: Organization
  private lateinit var organizationB: Organization

  @Before
  fun setup() = runBlocking {
    // Initialize fresh UserRepository for each test
    userRepository = UsersRepositoryLocal()
    repository = InvitationRepositoryLocal()

    // --- Create users ---
    admin = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
    member = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")
    outsider = User(id = "outsider", displayName = "Outsider", email = "outsider@example.com")

    // Register all users in the repository
    userRepository.newUser(admin)
    userRepository.newUser(member)
    userRepository.newUser(outsider)

    // --- Create organization ---
    organizationA =
        Organization(
            id = "orgA", name = "Org A")
    organizationB =
        Organization(id = "orgB", name = "Org B")

    // Set up user-organization relationships
    userRepository.addAdminToOrganization(admin.id, organizationA.id)
    userRepository.addUserToOrganization(member.id, organizationA.id)
    userRepository.addAdminToOrganization(admin.id, organizationB.id)

  }

  @Test
  fun insertAndRetrieveInvitation_success() = runBlocking {
    repository.insertInvitation(organizationA, admin)

    val retrievedList = repository.getAllInvitations()
    assertNotNull(retrievedList)
    assertEquals(1, retrievedList.size)
    assertEquals(organizationA.id, retrievedList.first().organizationId)
    assertEquals(InvitationStatus.Active, retrievedList.first().status)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertInvitationByNonAdmin_fails() = runBlocking { repository.insertInvitation(organizationA, member) }

  @Test
  fun updateInvitation_success() = runBlocking {
    repository.insertInvitation(organizationA, admin)
    val retrievedInv = repository.getAllInvitations().first()

    val updated = retrievedInv.copy(acceptedAt = Instant.now(), status = InvitationStatus.Used)
    repository.updateInvitation(retrievedInv.id, updated, organizationA, admin)

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
            organizationId = organizationA.id,
            code = "ABC123",
            status = InvitationStatus.Active),
        organizationA,
        admin)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateInvitationToActiveByMember_fails() = runBlocking {
    repository.insertInvitation(organizationA, admin)
    val retrievedInv = repository.getAllInvitations().first()
    val updated = retrievedInv.copy(status = InvitationStatus.Active)

    repository.updateInvitation(retrievedInv.id, updated, organizationA, member)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateInvitationToActiveByOutsider_fails() = runBlocking {
    repository.insertInvitation(organizationA, admin)
    val retrievedInv = repository.getAllInvitations().first()

    val updated = retrievedInv.copy(status = InvitationStatus.Active)

    repository.updateInvitation(retrievedInv.id, updated, organizationA, outsider)
  }

  @Test
  fun deleteInvitation_success() = runBlocking {
    repository.insertInvitation(organizationA, admin)
    val retrievedInv = repository.getAllInvitations().first()

    repository.deleteInvitation(retrievedInv.id, organizationA, admin)

    assertNull(repository.getInvitationById(retrievedInv.id))
    assertTrue(repository.getAllInvitations().isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteInvitationByNonAdmin_fails() = runBlocking {
    repository.insertInvitation(organizationA, admin)
    val retrievedInv = repository.getAllInvitations().first()

    repository.deleteInvitation(retrievedInv.id, organizationA, member)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteNonexistentInvitation_fails() = runBlocking {
    repository.deleteInvitation(UUID.randomUUID().toString(), organizationA, admin)
  }

  @Test
  fun getInvitationByOrganization_correctly_retrieves() = runBlocking {
    repository.insertInvitation(organizationA, admin)
    repository.insertInvitation(organizationB, admin)
    repository.insertInvitation(organizationB, admin)

    val invitationsOfOrgB = repository.getInvitationByOrganization(organizationB.id)

    assertEquals(2, invitationsOfOrgB.size)
  }
}
