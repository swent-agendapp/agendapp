package com.android.sample.model.organization.repository

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.constants.FirestoreConstants
import com.android.sample.model.constants.FirestoreConstants.COLLECTION_USERS
import com.android.sample.model.constants.FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.OrganizationMapper
import com.android.sample.model.organization.data.Organization
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of [OrganizationRepository] using Firestore as the backend.
 *
 * Notes:
 * - Methods `insertOrganization` and `updateOrganization` call `super` to perform admin checks
 *   defined in the interface.
 * - When overriding these methods, always call `super` first to ensure the admin check is applied.
 */
class OrganizationRepositoryFirebase(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : OrganizationRepository {

  override suspend fun getAllOrganizations(user: User): List<Organization> {
    val userDoc = db.collection(COLLECTION_USERS).document(user.id).get().await()
    val orgIds =
        (userDoc.get("organizations") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    if (orgIds.isEmpty()) return emptyList()
    val orgSnapshot =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .whereIn(FieldPath.documentId(), orgIds)
            .get()
            .await()
    return orgSnapshot.documents.mapNotNull { OrganizationMapper.fromDocument(it) }
  }

  override suspend fun insertOrganization(organization: Organization) {
    db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organization.id)
        .set(OrganizationMapper.toMap(model = organization))
        .await()
  }

  override suspend fun updateOrganization(
      organizationId: String,
      organization: Organization,
      user: User
  ) {
    val adminDocs =
        db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .collection(FirestoreConstants.COLLECTION_ADMINS)
            .get()
            .await()

    val isAdmin = adminDocs.documents.any { it.id == user.id }
    require(isAdmin) { "User ${user.id} is not an admin of organization $organizationId" }

    db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .set(OrganizationMapper.toMap(model = organization))
        .await()
  }

  override suspend fun deleteOrganization(organizationId: String, user: User) {
    val adminDocs =
        db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .collection(FirestoreConstants.COLLECTION_ADMINS)
            .get()
            .await()

    val isAdmin = adminDocs.documents.any { it.id == user.id }
    require(isAdmin) { "User ${user.id} is not an admin of organization $organizationId" }

    db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .delete()
        .await()
  }

  override suspend fun getOrganizationById(organizationId: String, user: User): Organization? {
    require(userRepository.getMembersIds(organizationId).contains(user.id))
    val document =
        db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .get()
            .await()
    val organization = OrganizationMapper.fromDocument(document = document)
    return organization
  }
}
