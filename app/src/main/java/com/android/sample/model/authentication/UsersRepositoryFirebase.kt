package com.android.sample.model.authentication

import com.android.sample.model.constants.FirestoreConstants.COLLECTION_USERS
import com.android.sample.model.firestoreMappers.UserMapper
import com.google.firebase.firestore.FirebaseFirestore
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

  override suspend fun getUsers(): List<User> {
    val snap = usersCollection().get().await()
    return snap.documents.mapNotNull { UserMapper.fromDocument(it) }
  }

  override suspend fun newUser(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }
    val data = UserMapper.toMap(user)
    usersCollection().document(user.id).set(data).await()
  }

  override suspend fun deleteUser(userId: String) {
    usersCollection().document(userId).delete().await()
  }
}
