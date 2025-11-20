package com.android.sample.model.organization

import com.android.sample.model.authentication.User
import com.android.sample.model.constants.FirestoreConstants
import com.android.sample.model.firestoreMappers.OrganizationMapper
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
class OrganizationRepositoryFirebase(private val db: FirebaseFirestore) : OrganizationRepository {

  override suspend fun getAllOrganizations(user: User): List<Organization> {
    val snapshot = db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH).get().await()

    val organizations = snapshot.mapNotNull { OrganizationMapper.fromDocument(document = it) }

    /*if (organizations.isNotEmpty()) {
      return organizations.filter { organization ->
        organization.admins.any { it.id == user.id } ||
            organization.members.any { it.id == user.id }
      }
    }*/
    return organizations
    // return emptyList()
  }

  override suspend fun insertOrganization(organization: Organization, user: User) {
    // Calls the interface check to ensure the user is an admin
    super.insertOrganization(organization, user)

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
    // Calls the interface check to ensure the user is an admin
    super.updateOrganization(organizationId, organization, user)

    db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .set(OrganizationMapper.toMap(model = organization))
        .await()
  }

  override suspend fun deleteOrganization(organizationId: String, user: User) {
    val document =
        db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .get()
            .await()
    val org =
        OrganizationMapper.fromDocument(document = document)
            ?: throw IllegalArgumentException("Organization does not exist")

    require(user.id in org.admins.map { it.id }) { "Only admins can delete the organization." }

    db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .delete()
        .await()
  }

  override suspend fun getOrganizationById(organizationId: String, user: User): Organization? {
    val document =
        db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .get()
            .await()
    val organization = OrganizationMapper.fromDocument(document = document)

    if (organization != null) {
      require(
          user.id in organization.admins.map { it.id } ||
              user.id in organization.members.map { it.id }) {
            "User does not have access to this organization."
          }
    }
    return organization
  }
}
