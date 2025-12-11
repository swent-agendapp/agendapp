package com.android.sample.model.organization.repository

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.organization.data.Organization
import kotlin.collections.plus

/**
 * Local in-memory implementation of [OrganizationRepository] for testing or offline usage.
 *
 * This implementation uses [UserRepositoryProvider] to check user permissions (admin/member access)
 * for various operations.
 */
class OrganizationRepositoryLocal(
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : OrganizationRepository {

  private val organizations: MutableList<Organization> = mutableListOf()

  override suspend fun getAllOrganizations(user: User): List<Organization> {
    // Filter organizations that exist in the repository AND the user has access to
    return user.organizations.mapNotNull { orgId -> organizations.find { it.id == orgId } }
  }

  override suspend fun insertOrganization(organization: Organization) {
    require(organizations.indexOfFirst { it.id == organization.id } == -1) {
      "Organization with id ${organization.id} already exists."
    }
    organizations.add(organization)
  }

  override suspend fun updateOrganization(
      organizationId: String,
      organization: Organization,
      user: User
  ) {
    val index = organizations.indexOfFirst { it.id == organizationId }
    require(index != -1) { "Organization with id $organizationId does not exist." }

    // Check if user is an admin
    val admins = userRepository.getAdminsIds(organizationId)
    require(admins.contains(user.id)) {
      "User ${user.id} is not an admin of organization $organizationId"
    }

    organizations[index] = organization
  }

  override suspend fun deleteOrganization(organizationId: String, user: User) {
    val organization = organizations.find { it.id == organizationId }
    require(organization != null) { "Organization with id $organizationId does not exist." }

    // Check if user is an admin
    val admins = userRepository.getAdminsIds(organizationId)
    require(admins.contains(user.id)) {
      "User ${user.id} is not an admin of organization $organizationId"
    }

    organizations.removeIf { it.id == organizationId }
  }

  override suspend fun getOrganizationById(organizationId: String, user: User): Organization? {
    val organization = organizations.find { it.id == organizationId }

    if (organization == null) {
      return null
    }

    // Check if user has access (is admin or member)
    val admins = userRepository.getAdminsIds(organizationId)
    val members = userRepository.getMembersIds(organizationId)

    require(admins.contains(user.id) || members.contains(user.id)) {
      "User ${user.id} does not have access to organization $organizationId"
    }

    return organization
  }
}
