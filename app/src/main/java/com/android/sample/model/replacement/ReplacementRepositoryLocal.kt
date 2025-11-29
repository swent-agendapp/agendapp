package com.android.sample.model.replacement

// Assisted by AI

class ReplacementRepositoryLocal() : ReplacementRepository {

  // In-memory storage for replacements : map of organization ID to list of replacements
  private val replacementsByOrganization: MutableMap<String, MutableList<Replacement>> =
      mutableMapOf()

  // Helper function to get the list of replacements for an organization or throw an error if it
  // doesn't exist
  private fun getListOrError(orgId: String): MutableList<Replacement> {
    val list = replacementsByOrganization[orgId]
    require(list != null) { "Organization $orgId does not exist." }
    return list
  }

  override suspend fun getAllReplacements(orgId: String): List<Replacement> {
    return replacementsByOrganization[orgId]?.toList() ?: emptyList()
  }

  override suspend fun insertReplacement(orgId: String, item: Replacement) {
    val list = replacementsByOrganization.getOrPut(orgId) { mutableListOf() }

    require(list.none { it.id == item.id }) { "Replacement with id ${item.id} already exists." }

    list.add(item)
  }

  override suspend fun updateReplacement(orgId: String, itemId: String, item: Replacement) {
    val list = getListOrError(orgId)

    val index = list.indexOfFirst { it.id == itemId }
    require(index != -1) { "Replacement with id $itemId does not exist." }

    list[index] = item
  }

  override suspend fun deleteReplacement(orgId: String, itemId: String) {
    val list = getListOrError(orgId)

    val index = list.indexOfFirst { it.id == itemId }
    require(index != -1) { "Replacement with id $itemId does not exist." }

    list.removeAt(index)
  }

  override suspend fun getReplacementById(orgId: String, itemId: String): Replacement? {
    return replacementsByOrganization[orgId]?.find { it.id == itemId }
  }

  override suspend fun getReplacementsByAbsentUser(
      orgId: String,
      userId: String
  ): List<Replacement> {
    return replacementsByOrganization[orgId]?.filter { it.absentUserId == userId } ?: emptyList()
  }

  override suspend fun getReplacementsBySubstituteUser(
      orgId: String,
      userId: String
  ): List<Replacement> {
    return replacementsByOrganization[orgId]?.filter { it.substituteUserId == userId }
        ?: emptyList()
  }

  override suspend fun getReplacementsByStatus(
      orgId: String,
      status: ReplacementStatus
  ): List<Replacement> {
    return replacementsByOrganization[orgId]?.filter { it.status == status } ?: emptyList()
  }
}
