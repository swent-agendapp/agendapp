package com.android.sample.model.organization.invitation

class InvitationRepositoryLocal : InvitationRepository {

  private val invitations = mutableListOf<Invitation>()

  override suspend fun getAllInvitations(): List<Invitation> {
    return invitations.toList()
  }

  override suspend fun insertInvitation(item: Invitation) {
    require(invitations.none { it.id == item.id }) {
      "Invitation with id ${item.id} already exists."
    }
    invitations.add(item)
  }

  override suspend fun updateInvitation(itemId: String, item: Invitation) {
    val index = invitations.indexOfFirst { it.id == itemId }
    require(index != -1) { "Invitation with id $itemId does not exist." }
    invitations[index] = item
  }

  override suspend fun deleteInvitation(itemId: String) {
    val index = invitations.indexOfFirst { it.id == itemId }
    require(index != -1) { "Invitation with id $itemId does not exist." }
    invitations.removeAt(index)
  }

  override suspend fun getInvitationById(itemId: String): Invitation? {
    return invitations.find { it.id == itemId }
  }
}
