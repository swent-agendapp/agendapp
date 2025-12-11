package com.android.sample.model.metadata

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.repository.OrganizationRepository

/**
 * In-memory fake repository used for unit/UI testing. Only implements the methods needed for
 * filters and simple flows.
 */
class FakeOrganizationRepository : OrganizationRepository {

  /** Storage: orgId -> Organization */
  private val organizations = mutableMapOf<String, Organization>()

  /** Control which user is considered “current user” during tests */
  var currentUser: User? = null

  // ----------------------------------------------------------
  // Helpers for test setup
  // ----------------------------------------------------------

  fun addOrganization(org: Organization) {
    organizations[org.id] = org
  }

  fun setMembers(orgId: String, members: List<User>) {
    val org = organizations[orgId]
    if (org != null) {
      organizations[orgId] = org.copy(members = members)
    }
  }

  // ----------------------------------------------------------
  // Required repository methods
  // ----------------------------------------------------------

  override suspend fun getAllOrganizations(user: User): List<Organization> {
    return organizations.values.filter { org ->
      org.members.contains(user) || org.admins.contains(user)
    }
  }

  override suspend fun insertOrganization(organization: Organization, user: User) {
    super.insertOrganization(organization, user) // performs admin check
    organizations[organization.id] = organization
  }

  override suspend fun updateOrganization(
      organizationId: String,
      organization: Organization,
      user: User
  ) {
    super.updateOrganization(organizationId, organization, user)
    organizations[organizationId] = organization
  }

  override suspend fun deleteOrganization(organizationId: String, user: User) {
    val org =
        organizations[organizationId]
            ?: throw IllegalArgumentException("Organization not found: $organizationId")

    require(org.admins.contains(user)) { "Only admins can delete an organization." }

    organizations.remove(organizationId)
  }

  override suspend fun getOrganizationById(organizationId: String, user: User): Organization? {
    val org = organizations[organizationId] ?: return null
    return if (org.members.contains(user) || org.admins.contains(user)) org else null
  }

  override suspend fun addMemberToOrganization(member: User, invitation: Invitation) {
    val orgId = invitation.organizationId
    val org =
        organizations[orgId] ?: throw IllegalArgumentException("No organization with id $orgId")

    require(!org.members.contains(member)) { "User already a member." }

    organizations[orgId] = org.copy(members = org.members + member)
  }
}
