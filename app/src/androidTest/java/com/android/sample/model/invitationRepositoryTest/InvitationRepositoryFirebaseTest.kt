package com.android.sample.model.invitationRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.InvitationRepositoryLocal
import com.android.sample.model.organization.invitation.InvitationStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvitationRepositoryLocalTest {

  private lateinit var repo: InvitationRepositoryLocal
  private lateinit var userRepo: UsersRepositoryLocal

  private lateinit var admin: User
  private lateinit var member: User
  private lateinit var outsider: User
  private lateinit var organization: Organization

  @Before
  fun setup() = runBlocking {
    userRepo = UsersRepositoryLocal()
    repo = InvitationRepositoryLocal(userRepo)

    admin = User(id = "adminA", email = "adminA@test.com", displayName = "Admin")
    member = User(id = "memberA", email = "member@test.com", displayName = "Member")
    outsider = User(id = "outsider", email = "outsider@test.com", displayName = "Outsider")

    userRepo.newUser(admin)
    userRepo.newUser(member)
    userRepo.newUser(outsider)

    organization = Organization(id = "orgA", name = "Org A")

    // Mark admin as org admin
    userRepo.addAdminToOrganization(admin.id, organization.id)
    // Member is not admin
    userRepo.addUserToOrganization(member.id, organization.id)
  }

  @Test
  fun admin_can_insert_invitation() = runBlocking {
    repo.insertInvitation(organization, admin)
    val retrieved = repo.getAllInvitations()

    assertEquals(1, retrieved.size)
    assertEquals(organization.id, retrieved.first().organizationId)
    assertEquals(InvitationStatus.Active, retrieved.first().status)
  }

  @Test
  fun admin_can_insert_two_invitations() = runBlocking {
    repo.insertInvitation(organization, admin)
    repo.insertInvitation(organization, admin)

    val all = repo.getAllInvitations()
    assertEquals(2, all.size)
  }

  @Test
  fun updateInvitation_modifies_fields() = runBlocking {
    repo.insertInvitation(organization, admin)
    val original = repo.getAllInvitations().first()

    val updated = original.copy(code = "UPDATED")
    repo.updateInvitation(original.id, updated, organization, admin)

    val retrieved = repo.getInvitationById(original.id)
    assertEquals("UPDATED", retrieved!!.code)
  }

  @Test
  fun updateInvitation_throws_when_ids_do_not_match() = runBlocking {
    repo.insertInvitation(organization, admin)
    val original = repo.getAllInvitations().first()

    val wrong = original.copy(id = "differentID")

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.updateInvitation(original.id, wrong, organization, admin) }
        }

    assertEquals(
        "Mismatched IDs: updated item id ${wrong.id} does not match target id ${original.id}",
        exception.message)
  }

  @Test
  fun nonAdmin_cannotInsertInvitation_throwsException() = runBlocking {
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.insertInvitation(organization, outsider) }
        }

    assertEquals("Only organization admins can create invitations.", exception.message)
  }

  @Test
  fun nonAdmin_cannotActivateInvitation() = runBlocking {
    repo.insertInvitation(organization, admin)
    val inv = repo.getAllInvitations().first()

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking {
            repo.updateInvitation(
                inv.id, inv.copy(status = InvitationStatus.Active), organization, member)
          }
        }

    assertEquals("Only organization admins can activate invitations.", exception.message)
  }

  @Test
  fun deleteInvitation_removes_document() = runBlocking {
    repo.insertInvitation(organization, admin)
    val inv = repo.getAllInvitations().first()

    repo.deleteInvitation(inv.id, organization, admin)

    assertNull(repo.getInvitationById(inv.id))
  }

  @Test
  fun nonAdmin_cannotDeleteInvitation_throwsException() = runBlocking {
    repo.insertInvitation(organization, admin)
    val inv = repo.getAllInvitations().first()

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.deleteInvitation(inv.id, organization, outsider) }
        }

    assertEquals("Only organization admins can delete invitations.", exception.message)
  }
}
