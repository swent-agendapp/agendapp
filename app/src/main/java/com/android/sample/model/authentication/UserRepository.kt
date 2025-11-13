package com.android.sample.model.authentication

/**
 * Represents persisted profile information for a user.
 *
 * Both the values supplied by Google and the ones edited inside the
 * application are stored so we can always fall back to Google defaults.
 */
data class UserProfile(
    val userId: String,
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val googleDisplayName: String? = null,
    val googleEmail: String? = null,
    val googlePhoneNumber: String? = null,
)

/** Repository responsible for persisting and retrieving user profile data. */
interface UserRepository {

  /** Returns the stored profile information for the given [userId], if any. */
  suspend fun getProfile(userId: String): UserProfile?

  /** Creates or updates the stored profile information for the given [profile.userId]. */
  suspend fun upsertProfile(profile: UserProfile)
}
