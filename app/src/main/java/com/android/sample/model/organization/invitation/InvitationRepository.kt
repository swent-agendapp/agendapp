package com.android.sample.model.organization.invitation

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization

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
   * @param Organization The organization for which an invitation will be inserted.
   * @param user The user attempting to perform the insertion.
   * @throws IllegalArgumentException if the user is not an organization admin.
   */
  suspend fun insertInvitation(organization: Organization, user: User) {
    require(organization.admins.contains(user)) {
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
   * @param organization The organization associated with the invitation.
   * @param user The user attempting to perform the update.
   * @throws IllegalArgumentException if the user does not have permission to activate the
   *   invitation.
   */
  suspend fun updateInvitation(
      itemId: String,
      item: Invitation,
      organization: Organization,
      user: User
  ) {
    require(item.id == itemId) {
      "Mismatched IDs: updated item id ${item.id} does not match target id $itemId"
    }
    val organizationId =
        getInvitationById(itemId)?.organizationId
            ?: throw IllegalArgumentException("Invitation with id $itemId does not exist.")
    require(organizationId == organization.id) {
      "Old invitation organizationId $organizationId does not match organization id ${organization.id}"
    }
    require(item.organizationId == organization.id) {
      "New invitation organizationId ${item.organizationId} does not match organization id ${organization.id}"
    }
    if (item.status == InvitationStatus.Active) {
      require(organization.admins.contains(user)) {
        "Only organization admins can activate invitations."
      }
    }
  }

  /**
   * Deletes an invitation from the repository.
   *
   * @param itemId The unique identifier of the invitation.
   * @param organization The organization associated with the invitation.
   * @param user The user attempting to perform the deletion.
   * @throws IllegalArgumentException if the itemId does not exist.
   */
  suspend fun deleteInvitation(itemId: String, organization: Organization, user: User) {
    val organizationId =
        getInvitationById(itemId)?.organizationId
            ?: throw IllegalArgumentException("Invitation with id $itemId does not exist.")

    require(organization.admins.contains(user)) {
      "Only organization admins can delete invitations."
    }
    require(organizationId == organization.id) {
      "Invitation organizationId $organizationId does not match organization id ${organization.id}"
    }
  }

  /**
   * Retrieves an invitation by its unique identifier.
   *
   * @param itemId The unique identifier.
   * @return The invitation if found, otherwise null.
   */
  suspend fun getInvitationById(itemId: String): Invitation?
}
