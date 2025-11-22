package com.android.sample.model.invitationRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
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

  private lateinit var inv1: Invitation
  private lateinit var inv2: Invitation

  @Before
  override fun setUp() {
    super.setUp()
    db = FirebaseFirestore.getInstance()
    repo = InvitationRepositoryFirebase(db)

    inv1 = Invitation.create(organizationId = "orgA")

    inv2 = Invitation.create(organizationId = "orgA")
  }

  @Test
  fun insert_and_getById_shouldReturnSame() = runBlocking {
    repo.insertInvitation(inv1)
    val retrieved = repo.getInvitationById(inv1.id)

    assertNotNull(retrieved)
    assertEquals(inv1.id, retrieved?.id)
    assertEquals(inv1.code, retrieved?.code)
    assertEquals(inv1.organizationId, retrieved?.organizationId)
  }

  @Test
  fun getAllInvitations_shouldReturnAllInserted() = runBlocking {
    repo.insertInvitation(inv1)
    repo.insertInvitation(inv2)

    val all = repo.getAllInvitations()
    assertEquals(2, all.size)
  }

  @Test
  fun updateInvitation_shouldModifyFields() = runBlocking {
    repo.insertInvitation(inv1)

    val updated = inv1.copy(code = "UPDATED")
    repo.updateInvitation(inv1.id, updated)

    val retrieved = repo.getInvitationById(inv1.id)
    assertEquals("UPDATED", retrieved?.code)
  }

  @Test
  fun updateInvitation_throws_when_IDs_do_not_match() = runBlocking {
    repo.insertInvitation(inv1)

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { repo.updateInvitation(inv1.id, inv2) }
        }

    assertEquals(
        "Mismatched IDs: updated item id ${inv2.id} does not match target id ${inv1.id}",
        exception.message)
  }

  @Test
  fun deleteInvitation_shouldRemoveDocument() = runBlocking {
    repo.insertInvitation(inv1)
    repo.deleteInvitation(inv1.id)

    assertNull(repo.getInvitationById(inv1.id))
  }

  @Test
  fun getInvitationByCode_shouldReturnCorrectInvitation() = runBlocking {
    repo.insertInvitation(inv1)

    val retrieved = repo.getInvitationByCode(inv1.code)
    assertNotNull(retrieved)
    assertEquals(inv1.id, retrieved?.id)
  }

  @Test
  fun getInvitationByCode_shouldReturnNull_ifMissing() = runBlocking {
    val missing = repo.getInvitationByCode("NOPE")
    assertNull(missing)
  }

  @Test
  fun getActiveInvitationsForOrganization_shouldReturnOnlyActive() = runBlocking {
    val used = inv2.copy(status = InvitationStatus.Used)

    repo.insertInvitation(inv1)
    repo.insertInvitation(used)

    val results = repo.getActiveInvitationsForOrganization("orgA")

    assertEquals(1, results.size)
    assertEquals(inv1.id, results.first().id)
  }

  @Test
  fun acceptInvitation_shouldSetUsedStatus_andEmail_andAcceptedAt() = runBlocking {
    repo.insertInvitation(inv1)

    repo.acceptInvitation(inv1.id, inviteeEmail = "test@mail.com")

    val updated = repo.getInvitationById(inv1.id)
    assertNotNull(updated)
    assertEquals(InvitationStatus.Used, updated!!.status)
    assertEquals("test@mail.com", updated.inviteeEmail)
    assertNotNull(updated.acceptedAt)
  }

  @Test
  fun expireInvitation_shouldSetExpiredStatus() = runBlocking {
    repo.insertInvitation(inv1)
    repo.expireInvitation(inv1.id)

    val updated = repo.getInvitationById(inv1.id)
    assertEquals(InvitationStatus.Expired, updated?.status)
  }

  @Test
  fun getInvitationsForOrganization_shouldReturnBothActiveAndUsed() = runBlocking {
    repo.insertInvitation(inv1)
    repo.insertInvitation(inv2.copy(status = InvitationStatus.Used))

    val list = repo.getInvitationsForOrganization("orgA")
    assertEquals(2, list.size)
  }

  @Test
  fun mapper_shouldHandleNullOptionalFields() = runBlocking {
    val inv =
        Invitation(
            id = "nulltest",
            organizationId = "orgA",
            code = "AAAAAA",
            createdAt = Instant.now(),
            acceptedAt = null,
            inviteeEmail = null,
            status = InvitationStatus.Active)

    repo.insertInvitation(inv)
    val retrieved = repo.getInvitationById("nulltest")

    assertNotNull(retrieved)
    assertNull(retrieved!!.acceptedAt)
    assertNull(retrieved.inviteeEmail)
  }
}
