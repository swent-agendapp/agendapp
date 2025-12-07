package com.android.sample.model.organization.invitation

class FakeInvitationRepository : InvitationRepository {

  private val invitations = mutableListOf<Invitation>()

  fun addInvitation(invitation: Invitation) {
    invitations.add(invitation)
  }

  override suspend fun getAllInvitations(): List<Invitation> {
    return invitations.toList()
  }

  override suspend fun getInvitationById(itemId: String): Invitation? {
    return invitations.firstOrNull { it.id == itemId }
  }

  override suspend fun getInvitationByOrganization(organizationId: String): List<Invitation> {
    return invitations.filter { it.organizationId == organizationId }
  }
}
