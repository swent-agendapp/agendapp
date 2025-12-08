package com.android.sample.model.organization.repository

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization

class FakeOrganizationRepository : OrganizationRepository {

  private val organizations = mutableMapOf<String, Organization>()

  override suspend fun insertOrganization(organization: Organization) {
    organizations[organization.id] = organization
  }

  override suspend fun updateOrganization(
    organizationId: String,
    organization: Organization,
    user: User
  ) {
    if(organizationId == organization.id)
      organizations[organizationId] = organization
  }

  override suspend fun getOrganizationById(organizationId: String, user: User): Organization? {
    return organizations[organizationId]
  }

  override suspend fun getAllOrganizations(user: User): List<Organization> {
    return organizations.values.toList()
  }

  override suspend fun deleteOrganization(organizationId: String, user: User) {
    organizations.remove(organizationId)
  }
}
