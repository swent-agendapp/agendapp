package com.android.sample.model.invitationRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.InvitationRepository
import com.android.sample.model.organization.invitation.InvitationRepositoryProvider
import com.android.sample.model.organization.invitation.InvitationStatus
import com.android.sample.utils.FirebaseEmulatedTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Tests written by AI

@RunWith(AndroidJUnit4::class)
class InvitationRepositoryFirebaseTest : FirebaseEmulatedTest() {

  private lateinit var repo: InvitationRepository

  private lateinit var admin: User
  private lateinit var member: User
  private lateinit var outsider: User
  private lateinit var organizationA: Organization
  private lateinit var organizationB: Organization

  @Before
  override fun setUp() = runBlocking {
    super.setUp()
    repo = InvitationRepositoryProvider.repository

    // Use local user repository for tests
    UserRepositoryProvider.repository = UsersRepositoryLocal()

    // --- Create users ---
    admin = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
    member = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")
    outsider = User(id = "outsider", displayName = "Outsider", email = "outsider@example.com")

    UserRepositoryProvider.repository.newUser(admin)
    UserRepositoryProvider.repository.newUser(member)
    UserRepositoryProvider.repository.newUser(outsider)

    // --- Create organization ---
    organizationA = Organization(id = "orgA", name = "Org A")

    organizationB = Organization(id = "orgB", name = "Org B")

    UserRepositoryProvider.repository.addAdminToOrganization(admin.id, organizationA.id)
    UserRepositoryProvider.repository.addUserToOrganization(member.id, organizationA.id)
    UserRepositoryProvider.repository.addAdminToOrganization(admin.id, organizationB.id)
  }

  @After
  fun cleanRepo() = runBlocking {

    // Remove users from the repository
    UserRepositoryProvider.repository.deleteUser(admin.id)
    UserRepositoryProvider.repository.deleteUser(member.id)
    UserRepositoryProvider.repository.deleteUser(outsider.id)
  }

  @Test
  fun admin_can_insert_invitation() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrieved = repo.getAllInvitations()

    assertEquals(1, retrieved.size)
    assertEquals(organizationA.id, retrieved.first().organizationId)
    assertEquals(InvitationStatus.Active, retrieved.first().status)
  }

  @Test
  fun admin_can_insert_and_getAllInvitations_shouldReturnAllInserted() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    repo.insertInvitation(organizationA, admin)

    val all = repo.getAllInvitations()
    assertEquals(2, all.size)
  }

  @Test
  fun updateInvitation_shouldModifyFields() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()

    val updated = retrievedInv.copy(code = "UPDATED")
    repo.updateInvitation(retrievedInv.id, updated, organizationA, admin)

    val retrieved = repo.getInvitationById(retrievedInv.id)
    assertEquals("UPDATED", retrieved?.code)
  }

  @Test
  fun updateInvitation_throws_when_IDs_do_not_match() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()
    val retrievedInvWithDifferentId = retrievedInv.copy(id = "anotherID")
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking {
            repo.updateInvitation(
                retrievedInv.id, retrievedInvWithDifferentId, organizationA, admin)
          }
        }

    assertEquals(
        "Mismatched IDs: updated item id ${retrievedInvWithDifferentId.id} does not match target id ${retrievedInv.id}",
        exception.message)
  }

  @Test
  fun nonAdmin_cannotInsertInvitation_throwsException() = runBlocking {
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.insertInvitation(organizationA, outsider) }
        }
    assertEquals("Only organization admins can create invitations.", exception.message)
  }

  @Test
  fun nonAdmin_cannotActivateInvitation_throwsException() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()
    repo.updateInvitation(
        retrievedInv.id, retrievedInv.copy(status = InvitationStatus.Used), organizationA, admin)
    val updatedInv = retrievedInv.copy(status = InvitationStatus.Active)
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.updateInvitation(retrievedInv.id, updatedInv, organizationA, member) }
        }
    assertEquals("Only organization admins can activate invitations.", exception.message)
  }

  @Test
  fun deleteInvitation_shouldRemoveDocument() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()
    repo.deleteInvitation(retrievedInv.id, organizationA, admin)

    assertNull(repo.getInvitationById(retrievedInv.id))
  }

  @Test
  fun nonAdmin_cannotDeleteInvitation_throwsException() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.deleteInvitation(retrievedInv.id, organizationA, outsider) }
        }
    assertEquals("Only organization admins can delete invitations.", exception.message)
  }

  @Test
  fun getInvitationByOrganization_correctly_retrieves() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    repo.insertInvitation(organizationB, admin)
    repo.insertInvitation(organizationB, admin)

    val invitationsOfOrgB = repo.getInvitationByOrganization(organizationB.id)

    assertEquals(2, invitationsOfOrgB.size)
  }

  @Test
  fun getInvitationByCode_correctly_retrieves() = runBlocking {
    repo.insertInvitation(organizationA, admin)
    val retrievedInv = repo.getAllInvitations().first()
    val fetchedByCode = repo.getInvitationByCode(retrievedInv.code)
    assertNotNull(fetchedByCode)
    assertEquals(retrievedInv.id, fetchedByCode?.id)
  }
}
