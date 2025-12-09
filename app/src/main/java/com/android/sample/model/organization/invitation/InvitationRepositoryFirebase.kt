package com.android.sample.model.organization.invitation

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.constants.FirestoreConstants.INVITATIONS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.InvitationMapper
import com.android.sample.model.organization.data.Organization
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InvitationRepositoryFirebase(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : InvitationRepository {

  private val collection
    get() = db.collection(INVITATIONS_COLLECTION_PATH)

  override suspend fun getAllInvitations(): List<Invitation> {
    val snapshot = collection.get().await()
    return snapshot.mapNotNull { InvitationMapper.fromDocument(it) }
  }

  override suspend fun insertInvitation(organization: Organization, user: User) {
    require(userRepository.getAdminsIds(organization.id).contains(user.id)) {
      // Match local repository message
      "Only organization admins can create invitations."
    }
    val item = Invitation.create(organizationId = organization.id)
    require(getInvitationById(item.id) == null) { "Invitation with id ${item.id} already exists." }
    collection.document(item.id).set(InvitationMapper.toMap(model = item)).await()
  }

  override suspend fun updateInvitation(
      itemId: String,
      item: Invitation,
      organization: Organization,
      user: User
  ) {
    // Interface-level checks
    super.updateInvitation(itemId, item, organization, user)

    // Must match the local repo message
    require(userRepository.getAdminsIds(organization.id).contains(user.id)) {
      "Only organization admins can activate invitations."
    }

    require(item.id == itemId) {
      // Same as local repository
      "Mismatched IDs: updated item id ${item.id} does not match target id $itemId"
    }

    collection.document(itemId).set(InvitationMapper.toMap(item)).await()
  }

  override suspend fun deleteInvitation(itemId: String, organization: Organization, user: User) {
    // Interface-level checks
    super.deleteInvitation(itemId, organization, user)

    // Must match InvitationRepositoryLocal
    require(userRepository.getAdminsIds(organization.id).contains(user.id)) {
      "Only organization admins can delete invitations."
    }

    collection.document(itemId).delete().await()
  }

  override suspend fun getInvitationById(itemId: String): Invitation? {
    val doc = collection.document(itemId).get().await()
    return InvitationMapper.fromDocument(doc)
  }
}
