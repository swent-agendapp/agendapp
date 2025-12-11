package com.android.sample.model.authentication

import java.util.UUID

/**
 * Data class representing a User.
 *
 * @property id Unique identifier for the User, generated as a UUID string by default.
 * @property displayName Name to display for User Profile, filled using a random animal name
 *   generator by default.
 * @property email Email address of the User.
 * @property phoneNumber Phone number of the User.
 * @property organizations List of organization IDs the User is associated with.
 *
 * Note : `User.id` uses `UUID.randomUUID()` as a default value, so each time a User object is
 * created without specifying an id, it will have a unique identifier. Hence Equality between two
 * newly created `User` instances is not expected or relied upon in the app.
 */
data class User(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String? = UsernameGenerator.generate(),
    val email: String? = null,
    val phoneNumber: String? = null,
    val organizations: List<String> = emptyList()
) {
  fun display(): String = displayName ?: email ?: "no name"
}
