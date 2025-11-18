package com.android.sample.model.authentication

import java.util.UUID

/**
 * Data class representing a User.
 *
 * @property id Unique identifier for the User, generated as a UUID string by default.
 * @property displayName Name to display for User Profile.
 * @property email Email address of the User.
 * @property phoneNumber Phone number of the User.
 */
data class User(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String?,
    val email: String?,
    val phoneNumber: String? = null,
)
