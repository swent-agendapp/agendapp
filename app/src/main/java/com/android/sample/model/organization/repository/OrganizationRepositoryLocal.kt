package com.android.sample.model.organization.repository

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.Invitation
import kotlin.collections.plus

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
    organizations[index] = organization
  }

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

  override suspend fun addMemberToOrganization(member: User, invitation: Invitation) {
    val organizationOfInvitation =
        organizations.find { it.id == invitation.organizationId }
            ?: throw IllegalArgumentException(
                "No organization matches the ID of the invitation's organizationId.")
    require(!(organizationOfInvitation.members.contains(member))) {
      "User is already a member of the organization."
    }

    val updatedOrganization =
        organizationOfInvitation.copy(members = organizationOfInvitation.members + member)
    val index = organizations.indexOfFirst { it.id == updatedOrganization.id }
    organizations[index] = updatedOrganization
  }
}
