package com.android.sample.model.authentication

/** Repository responsible for persisting and retrieving user profile data. */
interface UserRepository {

  /** Returns the stored profile information for the given [userId], if any. */
  suspend fun getUser(userId: String): User?

  /** Creates or updates the stored profile information for the given [User.id]. */
  suspend fun upsertUser(user: User)
}
