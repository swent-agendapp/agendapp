package com.android.sample.model.organization.repository

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.Invitation
import java.lang.IllegalArgumentException

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
    if (organizationId == organization.id) organizations[organizationId] = organization
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
    val organizationOfInvitation =
        organizations[invitation.organizationId]
            ?: throw IllegalArgumentException("Organizations does not exist")

    require(!(organizationOfInvitation.members.contains(member))) {
      "User is already a member of the organization."
    }
    val updatedMembers = organizationOfInvitation.members + member
    val updatedOrganization = organizationOfInvitation.copy(members = updatedMembers)
    organizations[organizationOfInvitation.id] = updatedOrganization
  }
}
