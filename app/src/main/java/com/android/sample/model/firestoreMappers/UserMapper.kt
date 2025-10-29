package com.android.sample.model.firestoreMappers

import com.android.sample.model.authentification.User
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [User] objects and vice versa. */
object UserMapper : FirestoreMapper<User> {

  override fun fromDocument(document: DocumentSnapshot): User? {
    val id = document.getString("id") ?: document.id
    val displayName = document.getString("displayName") ?: return null
    val email = document.getString("email") ?: return null

    return User(id = id, displayName = displayName, email = email)
  }

  override fun fromMap(data: Map<String, Any?>): User? {
    val id = data["id"] as? String ?: return null
    val displayName = data["displayName"] as? String ?: return null
    val email = data["email"] as? String ?: return null

    return User(id = id, displayName = displayName, email = email)
  }

  override fun toMap(model: User): Map<String, Any?> {
    return mapOf(
        "id" to model.id,
        "displayName" to model.displayName,
        "email" to model.email,
    )
  }
}
