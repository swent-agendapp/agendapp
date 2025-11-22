package com.android.sample.model.organization.invitation

import com.android.sample.model.constants.FirestoreConstants.INVITATIONS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.InvitationMapper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InvitationRepositoryFirebase(private val db: FirebaseFirestore) : InvitationRepository {

  private val collection
    get() = db.collection(INVITATIONS_COLLECTION_PATH)

  override suspend fun getAllInvitations(): List<Invitation> {
    val snapshot = collection.get().await()
    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }

  override suspend fun insertInvitation(item: Invitation) {
    collection.document(item.id).set(InvitationMapper.toMap(model = item)).await()
  }

  override suspend fun updateInvitation(itemId: String, item: Invitation) {
    require(item.id == itemId) {
      "Mismatched IDs: updated item id ${item.id} does not match target id $itemId"
    }
    collection.document(itemId).set(InvitationMapper.toMap(item)).await()
  }

  override suspend fun deleteInvitation(itemId: String) {
    collection.document(itemId).delete().await()
  }

  override suspend fun getInvitationById(itemId: String): Invitation? {
    val doc = collection.document(itemId).get().await()
    return InvitationMapper.fromDocument(doc)
  }

  /** Returns all ACTIVE invitations for a given organization */
  suspend fun getActiveInvitationsForOrganization(orgId: String): List<Invitation> {
    val snapshot =
        collection
            .whereEqualTo(InvitationMapper.ORGANIZATION_ID_FIELD, orgId)
            .whereEqualTo(InvitationMapper.STATUS_FIELD, InvitationStatus.Active.name)
            .get()
            .await()
    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }

  /** Finds an invitation by code (for join-by-code flow) */
  suspend fun getInvitationByCode(code: String): Invitation? {
    val snapshot = collection.whereEqualTo("code", code).limit(1).get().await()

    val doc = snapshot.documents.firstOrNull() ?: return null
    return InvitationMapper.fromDocument(doc)
  }

  /** Marks an invitation as used (atomic update). */
  suspend fun acceptInvitation(invitationId: String, inviteeEmail: String) {
    val update =
        mapOf(
            InvitationMapper.INVITEE_EMAIL_FIELD to inviteeEmail,
            InvitationMapper.ACCEPTED_AT_FIELD to Timestamp.now(),
            InvitationMapper.STATUS_FIELD to InvitationStatus.Used.name)

    collection.document(invitationId).update(update).await()
  }

  /** Marks an invitation as expired */
  suspend fun expireInvitation(invitationId: String) {
    val update = mapOf(InvitationMapper.STATUS_FIELD to InvitationStatus.Expired.name)
    collection.document(invitationId).update(update).await()
  }

  /** Returns all invitations for admin view (pending + used + expired) */
  suspend fun getInvitationsForOrganization(orgId: String): List<Invitation> {
    val snapshot =
        collection.whereEqualTo(InvitationMapper.ORGANIZATION_ID_FIELD, orgId).get().await()

    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }
}
