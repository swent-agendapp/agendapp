package com.android.sample.model.category

class EventCategoryRepositoryLocal() : EventCategoryRepository {
  private val categoryByOrganization: MutableMap<String, MutableList<EventCategory>> =
      mutableMapOf()

  override fun getNewUid(): String {
    return java.util.UUID.randomUUID().toString()
  }

  override suspend fun getAllCategories(orgId: String): List<EventCategory> {
    return categoryByOrganization[orgId] ?: emptyList()
  }

  override suspend fun insertCategory(orgId: String, item: EventCategory) {
    super.insertCategory(orgId, item)
    val list = categoryByOrganization.getOrPut(orgId) { mutableListOf() }

    require(list.none { it.id == item.id }) { "Category with id ${item.id} already exists." }

    list.add(item)
  }

  override suspend fun updateCategory(orgId: String, itemId: String, item: EventCategory) {
    super.updateCategory(orgId, itemId, item)
    val list =
        categoryByOrganization[orgId]
            ?: throw IllegalArgumentException("Organization with id ${orgId} not found")

    val index = list.indexOfFirst { it.id == itemId }
    require(index != -1) { "Category with id $itemId does not exist." }

    list[index] = item
  }

  override suspend fun deleteCategory(orgId: String, itemId: String) {
    val list =
        categoryByOrganization[orgId]
            ?: throw IllegalArgumentException("Organization with id ${orgId} not found")
    val index = list.indexOfFirst { it.id == itemId }
    require(index != 1) { "Item with id $itemId does not exist." }

    list.removeAt(index)
  }

  override suspend fun getCategoryById(orgId: String, itemId: String): EventCategory? {
    val retrievedCategory = categoryByOrganization[orgId]?.find { it.id == itemId }

    if (retrievedCategory == null) return null

    require(retrievedCategory.organizationId == orgId) {
      "Category's organizationId ${retrievedCategory.organizationId} does not match the provided orgId $orgId."
    }

    return retrievedCategory
  }
}
