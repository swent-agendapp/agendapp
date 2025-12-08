package com.android.sample.model.organization.repository

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization

/**
 * Local in-memory implementation of [OrganizationRepository] for testing or offline usage.
 *
 * Notes:
 * - Methods `insertOrganization` and `updateOrganization` call `super` to perform admin checks
 *   defined in the interface.
 * - When overriding these methods, always call `super` first to ensure the admin check is applied.
 */
class OrganizationRepositoryLocal : OrganizationRepository {

  private val organizations: MutableList<Organization> = mutableListOf()

  override suspend fun getAllOrganizations(user: User): List<Organization> {
    return user.organizations.map { id ->
      organizations.first { it -> it.id == id }
    }
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
    organizations[index] = organization
  }

  override suspend fun deleteOrganization(organizationId: String, user: User) {
    val organization = organizations.find { it.id == organizationId }
    require(organization != null) { "Organization with id $organizationId does not exist." }
    organizations.removeIf { it.id == organizationId }
  }

  override suspend fun getOrganizationById(organizationId: String, user: User): Organization? {
    val organization = organizations.find { it.id == organizationId }
    return organization
  }
}
