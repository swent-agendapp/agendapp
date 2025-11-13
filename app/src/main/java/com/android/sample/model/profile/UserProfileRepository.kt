package com.android.sample.model.profile

/**
 * Represents profile information stored for a user.
 *
 * Both Google-provided details and user-updated values are stored so that the
 * application can display either version when needed.
 */
data class UserProfileData(
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val googleDisplayName: String? = null,
    val googleEmail: String? = null,
    val googlePhoneNumber: String? = null,
)

/** Repository used to persist and retrieve profile information for users. */
interface UserProfileRepository {

  /** Retrieves the stored profile information for the given [userId], if any. */
  suspend fun getProfile(userId: String): UserProfileData?

  /**
   * Creates or updates the stored profile information for the given [userId].
   *
   * Only non-null fields of [profile] are merged into the existing document.
   */
  suspend fun upsertProfile(userId: String, profile: UserProfileData)
}
