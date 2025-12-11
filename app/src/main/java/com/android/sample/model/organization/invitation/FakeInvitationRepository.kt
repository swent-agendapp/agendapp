package com.android.sample.model.organization.invitation

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization

class FakeInvitationRepository(
    private val userRepository: UserRepository = UsersRepositoryLocal()
) : InvitationRepository {

  private val invitations = mutableListOf<Invitation>()

  fun addInvitation(invitation: Invitation) {
    invitations.add(invitation)
  }

  override suspend fun getAllInvitations(): List<Invitation> {
    return invitations.toList()
  }

  override suspend fun insertInvitation(organization: Organization, user: User) {
    val admins = userRepository.getAdminsIds(organization.id)
    require(user.id in admins) { "Only organization admins can activate invitations." }
    val item = Invitation.create(organizationId = organization.id)
    require(invitations.none { it.id == item.id }) {
      "Invitation with id ${item.id} already exists."
    }
    invitations.add(item)
  }

  override suspend fun updateInvitation(
      itemId: String,
      item: Invitation,
      organization: Organization,
      user: User
  ) {
    super.updateInvitation(itemId, item, organization, user)

    val index = invitations.indexOfFirst { it.id == itemId }
    require(index != -1) { "Invitation with id $itemId does not exist." }
    invitations[index] = item
  }

  override suspend fun deleteInvitation(itemId: String, organization: Organization, user: User) {
    super.deleteInvitation(itemId, organization, user)
    val admins = userRepository.getAdminsIds(organization.id)
    require(user.id in admins) { "Only organization admins can activate invitations." }

    val index = invitations.indexOfFirst { it.id == itemId }
    require(index != -1) { "Invitation with id $itemId does not exist." }
    invitations.removeAt(index)
  }


  override suspend fun getInvitationById(itemId: String): Invitation? {
    return invitations.firstOrNull { it.id == itemId }
  }

  override suspend fun getInvitationByOrganization(organizationId: String): List<Invitation> {
    return invitations.filter { it.organizationId == organizationId }
  }

  override suspend fun getInvitationByCode(code: String): Invitation? {
    return invitations.firstOrNull { it.code == code }
  }
}
