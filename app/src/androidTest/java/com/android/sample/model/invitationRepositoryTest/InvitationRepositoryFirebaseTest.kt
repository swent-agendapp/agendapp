package com.android.sample.model.invitationRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.InvitationRepositoryFirebase
import com.android.sample.model.organization.invitation.InvitationStatus
import com.android.sample.utils.FirebaseEmulatedTest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Tests written by AI

@RunWith(AndroidJUnit4::class)
class InvitationRepositoryFirebaseTest : FirebaseEmulatedTest() {

  private lateinit var repo: InvitationRepositoryFirebase
  private lateinit var db: FirebaseFirestore
  private lateinit var admin: User
  private lateinit var member: User
  private lateinit var outsider: User
  private lateinit var organization: Organization

  @Before
  override fun setUp() {
    super.setUp()
    db = FirebaseFirestore.getInstance()
    repo = InvitationRepositoryFirebase(db)

    // --- Create users ---
    admin = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
    member = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")
    outsider = User(id = "outsider", displayName = "Outsider", email = "outsider@example.com")

    // --- Create organization ---
    organization =
        Organization(
            id = "orgA",
            name = "Org A")
  }

  @Test
  fun admin_can_insert_invitation() = runBlocking {
    repo.insertInvitation(organization, admin)
    val retrieved = repo.getAllInvitations()

    assertNotNull(retrieved)
    assertEquals(1, retrieved.size)
    assertEquals(organization.id, retrieved.first().organizationId)
    assertEquals(InvitationStatus.Active, retrieved.first().status)
  }

  @Test
  fun admin_can_insert_and_getAllInvitations_shouldReturnAllInserted() = runBlocking {
    repo.insertInvitation(organization, admin)
    repo.insertInvitation(organization, admin)

    val all = repo.getAllInvitations()
    assertEquals(2, all.size)
  }

  @Test
  fun updateInvitation_shouldModifyFields() = runBlocking {
    repo.insertInvitation(organization, admin)
    val retrievedInv = repo.getAllInvitations().first()

    val updated = retrievedInv.copy(code = "UPDATED")
    repo.updateInvitation(retrievedInv.id, updated, organization, admin)

    val retrieved = repo.getInvitationById(retrievedInv.id)
    assertEquals("UPDATED", retrieved?.code)
  }

  @Test
  fun updateInvitation_throws_when_IDs_do_not_match() = runBlocking {
    repo.insertInvitation(organization, admin)
    val retrievedInv = repo.getAllInvitations().first()
    val retrievedInvWithDifferentId = retrievedInv.copy(id = "anotherID")
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking {
            repo.updateInvitation(retrievedInv.id, retrievedInvWithDifferentId, organization, admin)
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
          runBlocking { repo.insertInvitation(organization, outsider) }
        }
    assertEquals("Only organization admins can create invitations.", exception.message)
  }

  @Test
  fun nonAdmin_cannotActivateInvitation_throwsException() = runBlocking {
    repo.insertInvitation(organization, admin)
    val retrievedInv = repo.getAllInvitations().first()
    repo.updateInvitation(
        retrievedInv.id, retrievedInv.copy(status = InvitationStatus.Used), organization, member)
    val updatedInv = retrievedInv.copy(status = InvitationStatus.Active)
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.updateInvitation(retrievedInv.id, updatedInv, organization, member) }
        }
    assertEquals("Only organization admins can activate invitations.", exception.message)
  }

  @Test
  fun deleteInvitation_shouldRemoveDocument() = runBlocking {
    repo.insertInvitation(organization, admin)
    val retrievedInv = repo.getAllInvitations().first()
    repo.deleteInvitation(retrievedInv.id, organization, admin)

    assertNull(repo.getInvitationById(retrievedInv.id))
  }

  @Test
  fun nonAdmin_cannotDeleteInvitation_throwsException() = runBlocking {
    repo.insertInvitation(organization, admin)
    val retrievedInv = repo.getAllInvitations().first()

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.deleteInvitation(retrievedInv.id, organization, outsider) }
        }
    assertEquals("Only organization admins can delete invitations.", exception.message)
  }
}
