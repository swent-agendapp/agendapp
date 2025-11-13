package com.android.sample.model.authentication

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/** Firestore-backed implementation of [UserRepository]. */
class FirebaseUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

  private fun usersCollection() = firestore.collection("users")

  override suspend fun getUser(userId: String): User? {
    val snapshot = usersCollection().document(userId).get().await()
    if (!snapshot.exists()) return null

    return User(
        id = userId,
        displayName = snapshot.getString("displayName"),
        email = snapshot.getString("email"),
        phoneNumber = snapshot.getString("phoneNumber"),
        googleDisplayName = snapshot.getString("googleDisplayName"),
        googleEmail = snapshot.getString("googleEmail"),
        googlePhoneNumber = snapshot.getString("googlePhoneNumber"))
  }

  override suspend fun upsertUser(user: User) {
    val updates = mapOf(
        "displayName" to user.displayName,
        "email" to user.email,
        "phoneNumber" to user.phoneNumber,
        "googleDisplayName" to user.googleDisplayName,
        "googleEmail" to user.googleEmail,
        "googlePhoneNumber" to user.googlePhoneNumber)

    usersCollection().document(user.id).set(updates, SetOptions.merge()).await()
  }
}
