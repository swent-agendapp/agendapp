package com.android.sample.data.firebase.mappers

import com.android.sample.model.authentication.User
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [User] objects and vice versa. */
object UserMapper : FirestoreMapper<User> {

  override fun fromDocument(document: DocumentSnapshot): User? {
    if (!document.exists()) return null

    val id = document.getString("id") ?: document.id
    val displayName = document.getString("displayName")
    val email = document.getString("email")
    val phoneNumber = document.getString("phoneNumber")

    val organizations =
        document["organizations"]?.let { value ->
          when (value) {
            is List<*> -> value.filterIsInstance<String>()
            else -> emptyList()
          }
        } ?: emptyList()

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
    return if (model.organizations.isEmpty()) {
      mapOf(
          "id" to model.id,
          "displayName" to model.displayName,
          "email" to model.email,
          "phoneNumber" to model.phoneNumber,
      )
    } else {
      mapOf(
          "id" to model.id,
          "displayName" to model.displayName,
          "email" to model.email,
          "phoneNumber" to model.phoneNumber,
          "organizations" to model.organizations)
    }
  }
}
