package com.android.sample.model.firestoreMappers

import com.android.sample.model.authentication.User
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [User] objects and vice versa. */
object UserMapper : FirestoreMapper<User> {

  override fun fromDocument(document: DocumentSnapshot): User? {
    val id = document.getString("id") ?: document.id
    val displayName = document.getString("displayName")
    val email = document.getString("email")
    val phoneNumber = document.getString("phoneNumber")

    val organizations = document.get("organizations") as? List<String> ?: emptyList()

    return User(
        id = id,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber,
        organizations = organizations)
  }

  override fun fromMap(data: Map<String, Any?>): User? {
    val id = data["id"] as? String ?: return null
    val displayName = data["displayName"] as? String
    val email = data["email"] as? String
    val phoneNumber = data["phoneNumber"] as? String

    // Firestore maps arrays as List<Any?>
    val organizations =
        (data["organizations"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

    return User(
        id = id,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber,
        organizations = organizations)
  }

  override fun toMap(model: User): Map<String, Any?> {
    return mapOf(
        "id" to model.id,
        "displayName" to model.displayName,
        "email" to model.email,
        "phoneNumber" to model.phoneNumber,
        "organizations" to model.organizations)
  }
}
