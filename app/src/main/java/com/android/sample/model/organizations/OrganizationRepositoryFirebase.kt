package com.android.sample.model.organizations

import com.android.sample.model.authentification.User
import com.android.sample.model.constants.FirestoreConstants
import com.android.sample.model.map.Area
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.get
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

    val organizations = snapshot.mapNotNull { documentToOrganization(it) }

    if (organizations.isNotEmpty()) {
      return organizations.filter { organization ->
        organization.admins.any { it.id == user.id } ||
            organization.members.any { it.id == user.id }
      }
    }
    return emptyList()
  }

  override suspend fun insertOrganization(organization: Organization, user: User) {
    // Calls the interface check to ensure the user is an admin
    super.insertOrganization(organization, user)

    db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organization.id)
        .set(organizationToMap(organization))
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
        .set(organizationToMap(organization))
        .await()
  }

  override suspend fun deleteOrganization(organizationId: String, user: User) {
    val document =
        db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .get()
            .await()
    val org =
        documentToOrganization(document)
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
    val organization = documentToOrganization(document)

    if (organization != null) {
      require(
          user.id in organization.admins.map { it.id } ||
              user.id in organization.members.map { it.id }) {
            "User does not have access to this organization."
          }
    }
    return organization
  }

  private fun documentToOrganization(document: DocumentSnapshot): Organization? {
    val id = document.id
    val name = document.getString("name") ?: return null

    val adminsData = document["admins"] as? List<*> ?: emptyList<Any>()
    val membersData = document["members"] as? List<*> ?: emptyList<Any>()
    val areas = (document["areas"] as? List<*>)?.filterIsInstance<Area>() ?: emptyList()
    val geoCheckEnabled = document.getBoolean("geoCheckEnabled") ?: false

    val admins = adminsData.mapNotNull { documentToUser(it) }
    val members = membersData.mapNotNull { documentToUser(it) }

    return Organization(
        id = id,
        name = name,
        admins = admins,
        members = members,
        areas = areas,
        geoCheckEnabled = geoCheckEnabled)
  }

  private fun organizationToMap(organization: Organization): Map<String, Any?> {
    return mapOf(
        "name" to organization.name,
        "admins" to organization.admins.map { userToMap(it) },
        "members" to organization.members.map { userToMap(it) },
        "areas" to organization.areas,
        "geoCheckEnabled" to organization.geoCheckEnabled)
  }

  fun documentToUser(data: Any?): User? {
    val id = (data as? Map<*, *>)?.get("id") as? String ?: return null
    val displayName = data["displayName"] as? String ?: return null
    val email = data["email"] as? String ?: return null

    return User(id = id, displayName = displayName, email = email)
  }

  fun userToMap(user: User): Map<String, Any?> {
    return mapOf(
        "id" to user.id,
        "displayName" to user.displayName,
        "email" to user.email,
    )
  }
}
