package com.android.sample.utils

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import com.android.sample.model.organization.repository.SelectedOrganizationRepository

/**
 * Helper class for setting up organizations and users in Firebase-based tests.
 *
 * This class encapsulates the common setup pattern used across multiple UI tests:
 * - Creating multiple test users
 * - Creating a test organization
 * - Adding users to the organization
 * - Setting the selected organization
 *
 * Usage:
 * ```
 * val helper = OrganizationTestHelper()
 * val setup = runBlocking {
 *   helper.setupOrganizationWithUsers(
 *     organizationId = "testOrg",
 *     organizationName = "Test Organization",
 *     userCount = 4
 *   )
 * }
 * val users = setup.users
 * ```
 */
class OrganizationTestHelper {

  /**
   * Data class containing the results of the organization setup.
   *
   * @property users List of users that were created and added to the organization
   * @property organization The organization that was created
   * @property organizationId The ID of the organization
   */
  data class OrganizationSetup(
      val users: List<User>,
      val organization: Organization,
      val organizationId: String
  )

  /**
   * Sets up an organization with a specified number of users.
   *
   * Creates test users with predefined names and emails, creates an organization, adds all users to
   * the organization, and sets it as the selected organization.
   *
   * @param organizationId The ID to use for the organization
   * @param organizationName The name of the organization (defaults to "Test Organization")
   * @param userCount The number of users to create (defaults to 4)
   * @return OrganizationSetup containing the created users and organization
   */
  suspend fun setupOrganizationWithUsers(
      organizationId: String,
      organizationName: String = "Test Organization",
      userCount: Int = 4
  ): OrganizationSetup {
    // Create users with predefined test data
    val users = createTestUsers(userCount)

    // Add users to repository
    users.forEach { user -> UserRepositoryProvider.repository.newUser(user) }

    // Set selected organization
    SelectedOrganizationRepository.changeSelectedOrganization(organizationId)

    // Create organization
    val organization = Organization(name = organizationName, id = organizationId)

    OrganizationRepositoryProvider.repository.insertOrganization(organization)

    // Add all users to the organization
    users.forEach { user ->
      UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
    }

    return OrganizationSetup(
        users = users, organization = organization, organizationId = organizationId)
  }

  /**
   * Creates predefined test users.
   *
   * @param count Number of users to create (max 10 for predefined data)
   * @return List of User objects
   */
  private fun createTestUsers(count: Int): List<User> {
    val predefinedUsers =
        listOf(
            User(id = "1", displayName = "Alice", email = "alice@example.com"),
            User(id = "2", displayName = "Bob", email = "bob@example.com"),
            User(id = "3", displayName = "Charlie", email = "charlie@example.com"),
            User(id = "4", displayName = "Dana", email = "dana@example.com"),
            User(id = "5", displayName = "Eve", email = "eve@example.com"),
            User(id = "6", displayName = "Frank", email = "frank@example.com"),
            User(id = "7", displayName = "Grace", email = "grace@example.com"),
            User(id = "8", displayName = "Henry", email = "henry@example.com"),
            User(id = "9", displayName = "Ivy", email = "ivy@example.com"),
            User(id = "10", displayName = "Jack", email = "jack@example.com"))

    require(count <= predefinedUsers.size) {
      "Cannot create more than ${predefinedUsers.size} predefined users. Requested: $count"
    }

    return predefinedUsers.take(count)
  }

  /**
   * Creates custom test users with specified details.
   *
   * @param userDetails List of triples containing (id, displayName, email)
   * @return OrganizationSetup with the custom users
   */
  suspend fun setupOrganizationWithCustomUsers(
      organizationId: String,
      organizationName: String = "Test Organization",
      userDetails: List<Triple<String, String, String>>
  ): OrganizationSetup {
    // Create users from custom details
    val users =
        userDetails.map { (id, displayName, email) ->
          User(id = id, displayName = displayName, email = email)
        }

    // Add users to repository
    users.forEach { user -> UserRepositoryProvider.repository.newUser(user) }

    // Set selected organization
    SelectedOrganizationRepository.changeSelectedOrganization(organizationId)

    // Create organization
    val organization = Organization(name = organizationName, id = organizationId)

    OrganizationRepositoryProvider.repository.insertOrganization(organization)

    // Add all users to the organization
    users.forEach { user ->
      UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
    }

    return OrganizationSetup(
        users = users, organization = organization, organizationId = organizationId)
  }
}
