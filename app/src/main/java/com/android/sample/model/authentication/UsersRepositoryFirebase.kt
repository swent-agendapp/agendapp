package com.android.sample.model.authentication

import com.android.sample.model.constants.FirestoreConstants.COLLECTION_USERS
import com.android.sample.model.firestoreMappers.EmployeeMapper
import com.android.sample.model.firestoreMappers.UserMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation using a global collection: /users/{userId}
 *
 * Document fields : { "userId": "firebaseUid", "displayName": "John Doe", "email":
 * "abc@exemple.com",  "updatedAt": <timestamp> }
 */
class UserRepositoryFirebase(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository
) : EmployeeRepository {

  private fun employeesCol() = db.collection(COLLECTION_USERS)

  override suspend fun getUsers(): List<User> {
    val snap = employeesCol().get().await()
    return snap.documents.mapNotNull { EmployeeMapper.fromDocument(it) }
  }

  override suspend fun newEmployee(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }
    val data = UserMapper.toMap(user)
    employeesCol().document(user.id).set(data).await()
  }

  override suspend fun deleteEmployee(userId: String) {
    employeesCol().document(userId).delete().await()
  }
}
