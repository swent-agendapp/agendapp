package com.android.sample.model.organization.invitation

import com.android.sample.data.firebase.mappers.InvitationMapper
import com.android.sample.model.authentication.User
import com.android.sample.model.constants.FirestoreConstants.INVITATIONS_COLLECTION_PATH
import com.android.sample.model.organization.data.Organization
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InvitationRepositoryFirebase(private val db: FirebaseFirestore) : InvitationRepository {

  private val collection
    get() = db.collection(INVITATIONS_COLLECTION_PATH)

  override suspend fun getAllInvitations(): List<Invitation> {
    val snapshot = collection.get().await()
    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }

  override suspend fun insertInvitation(organization: Organization, user: User) {
    // Calls the interface check to ensure the user is an admin
    super.insertInvitation(organization, user)

    val item = Invitation.create(organizationId = organization.id)

    // Ensure no existing invitation has the same ID or code
    // Should never happen with proper UUIDs and very small chance for codes (P = 1/35^6)
    require(getInvitationById(item.id) == null) { "Invitation with id ${item.id} already exists." }
    require(getInvitationByCode(item.code) == null) { "Code collision, insertion aborted." }

    collection.document(item.id).set(InvitationMapper.toMap(model = item)).await()
  }

  override suspend fun updateInvitation(
      itemId: String,
      item: Invitation,
      organization: Organization,
      user: User
  ) {
    // Calls the interface check to ensure the user has the right to update the invitation
    super.updateInvitation(itemId, item, organization, user)
    require(item.id == itemId) {
      "Mismatched IDs: updated item id ${item.id} does not match target id $itemId"
    }
    collection.document(itemId).set(InvitationMapper.toMap(item)).await()
  }

  override suspend fun deleteInvitation(itemId: String, organization: Organization, user: User) {
    // Calls the interface check to ensure the user has the right to delete the invitation
    super.deleteInvitation(itemId, organization, user)
    collection.document(itemId).delete().await()
  }

  override suspend fun getInvitationById(itemId: String): Invitation? {
    val doc = collection.document(itemId).get().await()
    return InvitationMapper.fromDocument(doc)
  }

  override suspend fun getInvitationByOrganization(organizationId: String): List<Invitation> {
    return getAllInvitations().filter { it.organizationId == organizationId }
  }

  override suspend fun getInvitationByCode(code: String): Invitation? {
    val snapshot = collection.whereEqualTo(InvitationMapper.CODE_FIELD, code).get().await()

    val doc = snapshot.documents.firstOrNull() ?: return null
    return InvitationMapper.fromDocument(doc)
  }
}
