package com.android.sample.model.authentication

interface UserRepository {

  /**
   * @param organizationId the id of the organization we want the list of user
   * @return The list of users.
   */
  suspend fun getUsers(organizationId: String): List<User>

  /**
   * @param organizationId the id of the organization we want the list of admins
   * @return The list of admins.
   */
  suspend fun getAdmins(organizationId: String): List<User>

  /** Modify an existing user */
  suspend fun modifyUser(user: User)

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
