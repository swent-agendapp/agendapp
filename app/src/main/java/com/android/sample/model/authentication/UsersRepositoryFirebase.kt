package com.android.sample.model.authentication

import com.android.sample.model.constants.FirestoreConstants
import com.android.sample.model.constants.FirestoreConstants.COLLECTION_ADMINS
import com.android.sample.model.constants.FirestoreConstants.COLLECTION_USERS
import com.android.sample.model.constants.FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.UserMapper
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UsersRepositoryFirebase(
    private val db: FirebaseFirestore,
) : UserRepository {

  private fun usersCollection() = db.collection(COLLECTION_USERS)

  /** -------------------------- USERS IDS -------------------------- */
  override suspend fun getAdminsIds(organizationId: String): List<String> {
    val snap =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .collection(COLLECTION_ADMINS)
            .get()
            .await()

    return snap.documents.map { it.id }
  }

  override suspend fun getUsersIds(organizationId: String): List<String> {
    val snap =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .collection(COLLECTION_USERS)
            .get()
            .await()

    return snap.documents.map { it.id }
  }

  /** -------------------------- USERS DETAILS -------------------------- */
  override suspend fun getUsersByIds(userIds: List<String>): List<User> {
    val all = usersCollection().get().await().documents.mapNotNull { UserMapper.fromDocument(it) }

    return all.filter { userIds.contains(it.id) }
  }

  /** -------------------------- UPSERT / MODIFY -------------------------- */
  override suspend fun newUser(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }
    val data = UserMapper.toMap(user)

    usersCollection().document(user.id).set(data).await()
  }

  override suspend fun modifyUser(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }
    val data = UserMapper.toMap(user)

    usersCollection().document(user.id).set(data, SetOptions.merge()).await()
  }

  /** -------------------------- DELETE USER -------------------------- */
  override suspend fun deleteUser(userId: String) {
    val batch = db.batch()

    // Delete global user document
    batch.delete(usersCollection().document(userId))

    // Remove from all organizations
    val orgsSnapshot = db.collection(ORGANIZATIONS_COLLECTION_PATH).get().await()

    orgsSnapshot.documents.forEach { orgDoc ->
      val orgRef = db.collection(ORGANIZATIONS_COLLECTION_PATH).document(orgDoc.id)

      batch.delete(orgRef.collection(COLLECTION_USERS).document(userId))
      batch.delete(orgRef.collection(COLLECTION_ADMINS).document(userId))
    }

    batch.commit().await()
  }

  override suspend fun addUserToOrganization(userId: String, organizationId: String) {
    // 1. Add user to organization subcollection
    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .collection(COLLECTION_USERS)
        .document(userId)
        .set(mapOf("exists" to true))
        .await()

    // 2. Add organization to user.organizations array
    db.collection(FirestoreConstants.COLLECTION_USERS)
        .document(userId)
        .update("organizations", FieldValue.arrayUnion(organizationId))
        .await()
  }

  override suspend fun addAdminToOrganization(userId: String, organizationId: String) {
    // 1. Add user to organization admins subcollection
    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .collection(COLLECTION_ADMINS)
        .document(userId)
        .set(mapOf("exists" to true))
        .await()

    // 2. Add organization to user.organizations array
    db.collection(COLLECTION_USERS)
        .document(userId)
        .update("organizations", FieldValue.arrayUnion(organizationId))
        .await()
  }

  override suspend fun getUserById(userId: String): User? {
    val all = usersCollection().get().await().documents.mapNotNull { UserMapper.fromDocument(it) }
    return all.firstOrNull { it.id == userId }
  }
}
