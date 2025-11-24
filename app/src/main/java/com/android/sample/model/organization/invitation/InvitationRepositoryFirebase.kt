package com.android.sample.model.organization.invitation

import com.android.sample.model.authentication.User
import com.android.sample.model.constants.FirestoreConstants.INVITATIONS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.InvitationMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InvitationRepositoryFirebase(private val db: FirebaseFirestore) : InvitationRepository {

  private val collection
    get() = db.collection(INVITATIONS_COLLECTION_PATH)

  override suspend fun getAllInvitations(): List<Invitation> {
    val snapshot = collection.get().await()
    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }

  override suspend fun insertInvitation(item: Invitation, user: User) {
    // Calls the interface check to ensure the user is an admin
    super.insertInvitation(item, user)
    collection.document(item.id).set(InvitationMapper.toMap(model = item)).await()
  }

  override suspend fun updateInvitation(itemId: String, item: Invitation, user: User) {
    // Calls the interface check to ensure the user has the right to update the invitation
    super.updateInvitation(itemId, item, user)
    require(item.id == itemId) {
      "Mismatched IDs: updated item id ${item.id} does not match target id $itemId"
    }
    collection.document(itemId).set(InvitationMapper.toMap(item)).await()
  }

  override suspend fun deleteInvitation(itemId: String, user: User) {
    // Calls the interface check to ensure the user has the right to delete the invitation
    super.deleteInvitation(itemId, user)
    collection.document(itemId).delete().await()
  }

  override suspend fun getInvitationById(itemId: String): Invitation? {
    val doc = collection.document(itemId).get().await()
    return InvitationMapper.fromDocument(doc)
  }
}
