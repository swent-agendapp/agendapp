package com.android.sample.model.organization.invitation

import com.android.sample.model.authentication.User

interface InvitationRepository {

  /**
   * Retrieves all invitations from the repository.
   *
   * @return A list of all invitations.
   */
  suspend fun getAllInvitations(): List<Invitation>

  /**
   * Inserts a new invitation into the repository.
   *
   * Only organization admins are allowed to create invitations.
   *
   * If you override this method in a subclass, you should call `super.insertInvitation(...)` to
   *
   * @param item The invitation to be inserted.
   */
  suspend fun insertInvitation(item: Invitation, user: User) {
    require(item.organization.admins.contains(user)) {
      "Only organization admins can create invitations."
    }
  }

  /**
   * Updates an existing invitation.
   *
   * If the invitation status is being set to Active, only organization admins are allowed to
   * perform this action.
   *
   * If you override this method in a subclass, you should call `super.updateInvitation(...)` to
   * ensure the privilege check is performed.
   *
   * @param itemId The unique identifier of the invitation.
   * @param item The updated invitation object.
   */
  suspend fun updateInvitation(itemId: String, item: Invitation, user: User) {
    if (item.status == InvitationStatus.Active) {
      require(item.organization.admins.contains(user)) {
        "Only organization admins can activate invitations."
      }
    }
  }

  /**
   * Deletes an invitation from the repository.
   *
   * @param itemId The unique identifier of the invitation.
   * @throws IllegalArgumentException if the itemId does not exist.
   */
  suspend fun deleteInvitation(itemId: String) {}

  /**
   * Retrieves an invitation by its unique identifier.
   *
   * @param itemId The unique identifier.
   * @return The invitation if found, otherwise null.
   */
  suspend fun getInvitationById(itemId: String): Invitation?
}
