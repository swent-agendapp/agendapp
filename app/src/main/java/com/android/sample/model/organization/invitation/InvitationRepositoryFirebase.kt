package com.android.sample.model.organization.invitation

import com.android.sample.model.constants.FirestoreConstants.INVITATIONS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.InvitationMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InvitationRepositoryFirebase(private val db: FirebaseFirestore) : InvitationRepository {

  private val col
    get() = db.collection(INVITATIONS_COLLECTION_PATH)

  override suspend fun getAllInvitations(): List<Invitation> {
    val snapshot = col.get().await()
    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }

  override suspend fun insertInvitation(item: Invitation) {
    col.document(item.id).set(InvitationMapper.toMap(model = item)).await()
  }

  override suspend fun updateInvitation(itemId: String, item: Invitation) {
    require(item.id == itemId) {
      "Mismatched IDs: updated item id ${item.id} does not match target id $itemId"
    }
    col.document(itemId).set(InvitationMapper.toMap(item)).await()
  }

  override suspend fun deleteInvitation(itemId: String) {
    col.document(itemId).delete().await()
  }

  override suspend fun getInvitationById(itemId: String): Invitation? {
    val doc = col.document(itemId).get().await()
    return InvitationMapper.fromDocument(doc)
  }

  /** Returns all ACTIVE invitations for a given organization */
  suspend fun getActiveInvitationsForOrganization(orgId: String): List<Invitation> {
    val snapshot =
        col.whereEqualTo("organizationId", orgId)
            .whereEqualTo("status", InvitationStatus.Active.name)
            .get()
            .await()
    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }

  /** Finds an invitation by code (for join-by-code flow) */
  suspend fun getInvitationByCode(code: String): Invitation? {
    val snapshot = col.whereEqualTo("code", code).limit(1).get().await()

    val doc = snapshot.documents.firstOrNull() ?: return null
    return InvitationMapper.fromDocument(doc)
  }

  /** Marks an invitation as used (atomic update). */
  suspend fun acceptInvitation(invitationId: String, inviteeEmail: String) {
    val update =
        mapOf(
            "inviteeEmail" to inviteeEmail,
            "acceptedAt" to com.google.firebase.Timestamp.now(),
            "status" to InvitationStatus.Used.name)

    col.document(invitationId).update(update).await()
  }

  /** Marks an invitation as expired */
  suspend fun expireInvitation(invitationId: String) {
    val update = mapOf("status" to InvitationStatus.Expired.name)
    col.document(invitationId).update(update).await()
  }

  /** Returns all invitations for admin view (pending + used + expired) */
  suspend fun getInvitationsForOrganization(orgId: String): List<Invitation> {
    val snapshot = col.whereEqualTo("organizationId", orgId).get().await()

    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }
}
