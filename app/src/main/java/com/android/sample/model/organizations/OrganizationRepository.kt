package com.android.sample.model.organizations

import com.android.sample.model.authentification.User

/**
 * Repository interface for managing organizations.
 *
 * This interface defines methods for CRUD operations on organizations, with access control
 * enforced based on user roles (admins and members).
 */
interface OrganizationRepository {

  /**
   * Retrieves all organizations the user has access to.
   *
   * @param user The current user.
   * @return A list of organizations accessible by the user.
   */
  suspend fun getAllOrganizations(user: User): List<Organization>

  /**
   * Inserts a new organization into the repository.
   *
   * Precondition: the `user` must be included in `organization.admins`. This is enforced via a
   * `require` check.
   *
   * If you override this method in a subclass, you should call `super.insertOrganization(...)` to
   * ensure the admin check is performed.
   *
   * @param organization The organization to insert.
   * @param user The current user performing the action.
   * @throws IllegalArgumentException if the user is not an admin of the organization.
   */
  suspend fun insertOrganization(organization: Organization, user: User) {
    require(organization.admins.contains(user)) { "Only admins can insert a new organization." }
  }

  /**
   * Updates an existing organization in the repository.
   *
   * Precondition: the `user` must be included in `organization.admins`. This is enforced via a
   * `require` check.
   *
   * If you override this method in a subclass, you should call `super.updateOrganization(...)` to
   * ensure the admin check is performed.
   *
   * @param organizationId The ID of the organization to update.
   * @param organization The updated organization data.
   * @param user The current user performing the action.
   * @throws IllegalArgumentException if the user is not an admin of the organization.
   */
  suspend fun updateOrganization(organizationId: String, organization: Organization, user: User) {
    require(organization.admins.contains(user)) { "Only admins can update the organization." }
  }

  /**
   * Deletes an organization from the repository.
   *
   * Only admins of the organization can delete it.
   *
   * Implementations must perform an admin check (similar to insert/update). If you override this
   * method, make sure to call `super.deleteOrganization(...)` if a default admin check is provided,
   * or implement your own check.
   *
   * @param organizationId The ID of the organization to delete.
   * @param user The current user performing the action.
   * @throws IllegalArgumentException if the organizationId does not exist or user is not admin.
   */
  suspend fun deleteOrganization(organizationId: String, user: User)

  /**
   * Retrieves an organization by its unique identifier.
   *
   * Implementations should ensure the user has access rights to this organization.
   *
   * @param organizationId The unique identifier of the organization.
   * @param user The current user performing the action.
   * @return The organization if found and accessible, or null if not found or unauthorized.
   */
  suspend fun getOrganizationById(organizationId: String, user: User): Organization?
}
