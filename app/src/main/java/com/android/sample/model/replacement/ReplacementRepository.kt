package com.android.sample.model.replacement

// Assisted by AI

interface ReplacementRepository {
  /**
   * Retrieves all replacement requests from the repository.
   *
   * @return A list of all replacement requests.
   */
  suspend fun getAllReplacements(orgId: String): List<Replacement>

  /**
   * Inserts a new replacement request into the repository.
   *
   * @param item The replacement request to be inserted.
   */
  suspend fun insertReplacement(orgId: String, item: Replacement)

  /**
   * Updates an existing replacement request in the repository.
   *
   * @param itemId The unique identifier of the replacement request to update.
   * @param item The updated replacement data.
   */
  suspend fun updateReplacement(orgId: String, itemId: String, item: Replacement)

  /**
   * Deletes a replacement request from the repository.
   *
   * @param itemId The unique identifier of the replacement request to delete.
   * @throws IllegalArgumentException if the itemId does not exist.
   */
  suspend fun deleteReplacement(orgId: String, itemId: String)

  /**
   * Retrieves a replacement request by its unique identifier.
   *
   * @param itemId The unique identifier of the replacement request.
   * @return The replacement request if found, or null if not found.
   */
  suspend fun getReplacementById(orgId: String, itemId: String): Replacement?

  /**
   * Retrieves all replacement requests associated with a specific absent user.
   *
   * @param userId The ID of the absent user whose replacement requests to retrieve.
   * @return A list of replacement requests where the specified user is the absent user.
   */
  suspend fun getReplacementsByAbsentUser(orgId: String, userId: String): List<Replacement>

  /**
   * Retrieves all replacement requests associated with a specific substitute user.
   *
   * @param userId The ID of the substitute user whose replacement requests to retrieve.
   * @return A list of replacement requests where the specified user is the substitute user.
   */
  suspend fun getReplacementsBySubstituteUser(orgId: String, userId: String): List<Replacement>

  /**
   * Retrieves all replacement requests for a given status (e.g., Pending, Accepted, Declined).
   *
   * @param status The status of the replacement requests to retrieve.
   * @return A list of replacement requests with the specified status.
   */
  suspend fun getReplacementsByStatus(orgId: String, status: ReplacementStatus): List<Replacement>
}
