package com.android.sample.model.authentication

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/** Firestore-backed implementation of [UserRepository]. */
class FirebaseUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

  private fun usersCollection() = firestore.collection("users")

  override suspend fun getProfile(userId: String): UserProfile? {
    val snapshot = usersCollection().document(userId).get().await()
    if (!snapshot.exists()) return null

    return UserProfile(
        userId = userId,
        displayName = snapshot.getString("displayName"),
        email = snapshot.getString("email"),
        phoneNumber = snapshot.getString("phoneNumber"),
        googleDisplayName = snapshot.getString("googleDisplayName"),
        googleEmail = snapshot.getString("googleEmail"),
        googlePhoneNumber = snapshot.getString("googlePhoneNumber"))
  }

  override suspend fun upsertProfile(profile: UserProfile) {
    val updates = mutableMapOf<String, Any?>()

    profile.displayName?.let { updates["displayName"] = it }
    profile.email?.let { updates["email"] = it }
    profile.phoneNumber?.let { updates["phoneNumber"] = it }
    profile.googleDisplayName?.let { updates["googleDisplayName"] = it }
    profile.googleEmail?.let { updates["googleEmail"] = it }
    profile.googlePhoneNumber?.let { updates["googlePhoneNumber"] = it }

    if (updates.isEmpty()) return

    usersCollection().document(profile.userId).set(updates, SetOptions.merge()).await()
  }
}
