package com.android.sample.model.authentication

interface UserRepository {

  /** @return The list of users. */
  suspend fun getUsers(): List<User>

  /**
   * Create or update an user
   *
   * @param user The user to create
   */
  suspend fun newUser(user: User)

  /**
   * Delete an user by ID
   *
   * @param userId The user ID to delete
   */
  suspend fun deleteUser(userId: String)
}
