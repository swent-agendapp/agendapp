package com.android.sample.model.category

interface EventCategoryRepository {
  fun getNewUid(): String

  /**
   * Retrieves all category items from the repository.
   *
   * @param orgId The organization ID to which the categories belong.
   * @return A list of all category items.
   */
  suspend fun getAllCategories(orgId: String): List<EventCategory>

  /**
   * Inserts a new category item into the repository.
   *
   * Note: Implementations have to call super.insertCategory to perform the organizationId check
   *
   * @param orgId The organization ID to which the category belongs.
   * @param item The category item to be inserted.
   */
  suspend fun insertCategory(orgId: String, item: EventCategory) {
    require(item.organizationId == orgId) {
      "Category's organizationId ${item.organizationId} does not match the provided orgId $orgId."
    }
  }

  /**
   * Updates an existing category item in the repository.
   *
   * Note: Implementations have to call super.updateEvent to perform the organizationId check
   *
   * @param orgId The organization ID to which the category belongs.
   * @param itemId The unique identifier of the category item to be updated.
   * @param item The category item to be updated.
   */
  suspend fun updateCategory(orgId: String, itemId: String, item: EventCategory) {
    require(item.organizationId == orgId) {
      "Event's organizationId ${item.organizationId} does not match the provided orgId $orgId."
    }
  }

  /**
   * Deletes a category item from the repository.
   *
   * @param orgId The organization ID to which the category belongs.
   * @param itemId The category item to be deleted.
   * @throws IllegalArgumentException if the itemId does not exist.
   */
  suspend fun deleteCategory(orgId: String, itemId: String)

  /**
   * Retrieves a category item by its unique identifier.
   *
   * @param orgId The organization ID to which the category belongs.
   * @param itemId The unique identifier of the category item.
   * @return The category item if found, or null if not found.
   */
  suspend fun getCategoryById(orgId: String, itemId: String): EventCategory?
}
