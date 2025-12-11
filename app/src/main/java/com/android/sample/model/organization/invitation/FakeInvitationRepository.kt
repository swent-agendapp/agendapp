package com.android.sample.model.organization.invitation

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization

class FakeInvitationRepository : InvitationRepository {

  private val invitations = mutableListOf<Invitation>()

  fun addInvitation(invitation: Invitation) {
    invitations.add(invitation)
  }

  override suspend fun getAllInvitations(): List<Invitation> {
    return invitations.toList()
  }

  override suspend fun insertInvitation(organization: Organization, user: User) {
    val item = Invitation.create(organizationId = organization.id)
    require(invitations.none { it.id == item.id }) {
      "Invitation with id ${item.id} already exists."
    }
    invitations.add(item)
  }

  override suspend fun getInvitationById(itemId: String): Invitation? {
    return invitations.firstOrNull { it.id == itemId }
  }

  override suspend fun getInvitationByOrganization(organizationId: String): List<Invitation> {
    return invitations.filter { it.organizationId == organizationId }
  }
}
