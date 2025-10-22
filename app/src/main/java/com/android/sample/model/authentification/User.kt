package com.android.sample.model.authentification

/**
 * Data class representing a User.
 *
 * @property id Unique identifier for the User.
 * @property displayName Name to display for User Profile.
 * @property email Email address of the User.
 * @property phoneNumber Phone number of the User.
 */
data class User(
    val id: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String? = null,
)
