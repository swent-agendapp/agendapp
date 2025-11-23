package com.android.sample.model.organization.invitation

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
   * @param item The invitation to be inserted.
   */
  suspend fun insertInvitation(item: Invitation)

  /**
   * Updates an existing invitation.
   *
   * @param itemId The unique identifier of the invitation.
   * @param item The updated invitation object.
   */
  suspend fun updateInvitation(itemId: String, item: Invitation)

  /**
   * Deletes an invitation from the repository.
   *
   * @param itemId The unique identifier of the invitation.
   * @throws IllegalArgumentException if the itemId does not exist.
   */
  suspend fun deleteInvitation(itemId: String)

  /**
   * Retrieves an invitation by its unique identifier.
   *
   * @param itemId The unique identifier.
   * @return The invitation if found, otherwise null.
   */
  suspend fun getInvitationById(itemId: String): Invitation?
}
