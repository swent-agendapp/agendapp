package com.android.sample.model.organization

import com.android.sample.model.authentication.User

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
    return organizations.filter { organization ->
      organization.admins.contains(user) || organization.members.contains(user)
    }
  }

  override suspend fun insertOrganization(organization: Organization, user: User) {
    // Calls the interface check to ensure the user is an admin
    super.insertOrganization(organization, user)

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
    // Calls the interface check to ensure the user is an admin
    super.updateOrganization(organizationId, organization, user)

    val index = organizations.indexOfFirst { it.id == organizationId }
    require(index != -1) { "Organization with id $organizationId does not exist." }
    organizations[index] = organization.copy(version = System.currentTimeMillis())
  }

    //Later : do not delete the organization, but mark it as deleted with a flag.
  override suspend fun deleteOrganization(organizationId: String, user: User) {
    val organization = organizations.find { it.id == organizationId }
    require(organization != null) { "Organization with id $organizationId does not exist." }
    require(organization.admins.contains(user)) { "Only admins can delete the organization." }
    organizations.removeIf { it.id == organizationId }
  }

  override suspend fun getOrganizationById(organizationId: String, user: User): Organization? {
    val organization = organizations.find { it.id == organizationId }
    if (organization != null) {
      require(organization.admins.contains(user) || organization.members.contains(user)) {
        "User does not have access to this organization."
      }
    }
    return organization
  }
}
