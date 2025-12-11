package com.android.sample.model.organization.repository

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.Invitation

class FakeOrganizationRepository : OrganizationRepository {

  private val organizations = mutableMapOf<String, Organization>()

  fun addOrganization(org: Organization) {
    organizations[org.id] = org
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

  override suspend fun addMemberToOrganization(member: User, invitation: Invitation) {
    val organization = organizations[invitation.organizationId] ?: return
    val updatedMembers = organization.members + member
    val updatedOrganization = organization.copy(members = updatedMembers)
    organizations[organization.id] = updatedOrganization
  }
}
