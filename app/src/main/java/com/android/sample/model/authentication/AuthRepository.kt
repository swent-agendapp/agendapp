package com.android.sample.model.authentication

import androidx.credentials.Credential

/** Handles authentication operations such as signing in with Google and signing out. */
interface AuthRepository {

  /**
   * Signs in the user using a Google account through the Credential Manager API.
   *
   * @return A [Result] containing a [User] on success, or an exception on failure.
   */
  suspend fun signInWithGoogle(credential: Credential): Result<User>

  /**
   * Signs out the currently authenticated user and clears the credential state.
   *
   * @return A [Result] indicating success or failure.
   */
  fun signOut(): Result<Unit>

  /**
   * Gets the currently authenticated user from persisted session.
   *
   * @return The currently authenticated [User], or null if not signed in.
   */
  fun getCurrentUser(): User?

  /**
   * Fetches a user by ID from the repository using a filtered query.
   *
   * @param userId The ID of the user to retrieve.
   * @return The [User] matching the given ID, or null if not found.
   */
  suspend fun getUserById(userId: String): User?
}
