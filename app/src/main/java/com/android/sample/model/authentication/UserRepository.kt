package com.android.sample.model.authentication

interface UserRepository {

  /**
   * @param organizationId the id of the organization we want the list of user
   * @return The list of users of the organization.
   */
  suspend fun getMembersIds(organizationId: String): List<String>

  /**
   * @param organizationId the id of the organization we want the list of admins
   * @return The list of admins.
   */
  suspend fun getAdminsIds(organizationId: String): List<String>

  /**
   * @param userIds the list of user IDs to retrieve
   * @return The list of users corresponding to the provided IDs.
   */
  suspend fun getUsersByIds(userIds: List<String>): List<User>


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

  /**
   * Add a user to an organization
   *
   * @param userId The user ID to add
   * @param orgId The organization ID to which the user will be added
   */
  suspend fun addUserToOrganization(userId: String, organizationId: String)

  /**
   * Add a user as an admin to an organization
   *
   * @param userId The user ID to add as admin
   * @param organizationId The organization ID to which the admin will be added
   */
  suspend fun addAdminToOrganization(userId: String, organizationId: String)
}
