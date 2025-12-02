package com.android.sample.model.authentication

import com.android.sample.model.constants.FirestoreConstants
import com.android.sample.model.constants.FirestoreConstants.COLLECTION_ADMINS
import com.android.sample.model.constants.FirestoreConstants.COLLECTION_USERS
import com.android.sample.model.firestoreMappers.UserMapper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation using a global collection: /users/{userId}
 *
 * Document fields : { "userId": "firebaseUid", "displayName": "John Doe", "email":
 * "johndoe@exemple.com", "updatedAt": <timestamp> }
 */
class UsersRepositoryFirebase(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository
) : UserRepository {

  private fun usersCollection() = db.collection(COLLECTION_USERS)

  override suspend fun getAdmins(organizationId: String): List<User> {
    val snap =
        db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .collection(COLLECTION_ADMINS)
            .get()
            .await()
    return snap.documents.mapNotNull { UserMapper.fromDocument(it) }
  }

  override suspend fun getUsers(organizationId: String): List<User> {
    val snap =
        db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
            .document(organizationId)
            .collection(COLLECTION_USERS)
            .get()
            .await()
    return snap.documents.mapNotNull { UserMapper.fromDocument(it) }
  }

  override suspend fun newUser(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }
    val data = UserMapper.toMap(user)
    usersCollection().document(user.id).set(data).await()
  }
  // Modify an existing user, only specified fields are updated
  override suspend fun modifyUser(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }

    val data = UserMapper.toMap(user)

    usersCollection().document(user.id).set(data, SetOptions.merge()).await()
  }

  // Delete user from /users and from all organizations they belong to
  override suspend fun deleteUser(userId: String) {
    // Load user to know which organizations they belong to
    val userSnap = usersCollection().document(userId).get().await()
    val user = UserMapper.fromDocument(userSnap) ?: return

    // Start batch delete
    val batch = db.batch()

    // Delete user document in /users/{userId}
    batch.delete(usersCollection().document(userId))

    // Remove from each organization
    user.organizations.forEach { orgId ->
      val orgDoc = db.collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH).document(orgId)

      // Delete from users subcollection
      batch.delete(orgDoc.collection(COLLECTION_USERS).document(userId))

      // Delete from admins subcollection (if present)
      batch.delete(orgDoc.collection(COLLECTION_ADMINS).document(userId))
    }

    // Commit all updates
    batch.commit().await()
  }
}
