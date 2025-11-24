package com.android.sample.model.invitationRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.Organization
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationRepositoryFirebase
import com.android.sample.model.organization.invitation.InvitationStatus
import com.android.sample.utils.FirebaseEmulatedTest
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
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
  private lateinit var orgA: Organization
  private lateinit var inv1: Invitation
  private lateinit var inv2: Invitation

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
    orgA =
        Organization(
            id = "orgA", name = "Org A", admins = listOf(admin), members = listOf(member, admin))

    inv1 = Invitation.create(organization = orgA)

    inv2 = Invitation.create(organization = orgA)
  }

  @Test
  fun admin_can_insert_and_getById_shouldReturnSame() = runBlocking {
    repo.insertInvitation(inv1, admin)
    val retrieved = repo.getInvitationById(inv1.id)

    assertNotNull(retrieved)
    assertEquals(inv1.id, retrieved?.id)
    assertEquals(inv1.code, retrieved?.code)
    assertEquals(inv1.organization, retrieved?.organization)
  }

  @Test
  fun admin_can_insert_and_getAllInvitations_shouldReturnAllInserted() = runBlocking {
    repo.insertInvitation(inv1, admin)
    repo.insertInvitation(inv2, admin)

    val all = repo.getAllInvitations()
    assertEquals(2, all.size)
  }

  @Test
  fun updateInvitation_shouldModifyFields() = runBlocking {
    repo.insertInvitation(inv1, admin)

    val updated = inv1.copy(code = "UPDATED")
    repo.updateInvitation(inv1.id, updated, admin)

    val retrieved = repo.getInvitationById(inv1.id)
    assertEquals("UPDATED", retrieved?.code)
  }

  @Test
  fun updateInvitation_throws_when_IDs_do_not_match() = runBlocking {
    repo.insertInvitation(inv1, admin)

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.updateInvitation(inv1.id, inv2, admin) }
        }

    assertEquals(
        "Mismatched IDs: updated item id ${inv2.id} does not match target id ${inv1.id}",
        exception.message)
  }

  @Test
  fun nonAdmin_cannotInsertInvitation_throwsException() = runBlocking {
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.insertInvitation(inv1, outsider) }
        }
    assertEquals("Only organization admins can create invitations.", exception.message)
  }

  @Test
  fun nonAdmin_cannotActivateInvitation_throwsException() = runBlocking {
    repo.insertInvitation(inv1.copy(status = InvitationStatus.Used), admin)
    val updated = inv1.copy(status = InvitationStatus.Active)
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.updateInvitation(inv1.id, updated, outsider) }
        }
    assertEquals("Only organization admins can activate invitations.", exception.message)
  }

  @Test
  fun deleteInvitation_shouldRemoveDocument() = runBlocking {
    repo.insertInvitation(inv1, admin)
    repo.deleteInvitation(inv1.id, admin)

    assertNull(repo.getInvitationById(inv1.id))
  }

  @Test
  fun nonAdmin_cannotDeleteInvitation_throwsException() = runBlocking {
    repo.insertInvitation(inv1, admin)
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.deleteInvitation(inv1.id, outsider) }
        }
    assertEquals("Only organization admins can delete invitations.", exception.message)
  }

  @Test
  fun mapper_shouldHandleNullOptionalFields() = runBlocking {
    val inv =
        Invitation(
            id = "nulltest",
            organization = orgA,
            code = "AAAAAA",
            createdAt = Instant.now(),
            acceptedAt = null,
            inviteeEmail = null,
            status = InvitationStatus.Active)

    repo.insertInvitation(inv, admin)
    val retrieved = repo.getInvitationById("nulltest")

    assertNotNull(retrieved)
    assertNull(retrieved!!.acceptedAt)
    assertNull(retrieved.inviteeEmail)
  }
}
