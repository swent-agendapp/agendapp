package com.android.sample.model.replacement

// Assisted by AI

class ReplacementRepositoryLocal() : ReplacementRepository {

  private val replacements: MutableList<Replacement> = mutableListOf()

  override suspend fun getAllReplacements(): List<Replacement> {
    return replacements.toList()
  }

  override suspend fun insertReplacement(item: Replacement) {
    require(replacements.indexOfFirst { it.id == item.id } == -1) {
      "Replacement with id ${item.id} already exists."
    }
    replacements.add(item)
  }

  override suspend fun updateReplacement(itemId: String, item: Replacement) {
    val index = replacements.indexOfFirst { it.id == itemId }
    require(index != -1) { "Replacement with id $itemId does not exist." }
    replacements[index] = item
  }

  override suspend fun deleteReplacement(itemId: String) {
    val index = replacements.indexOfFirst { it.id == itemId }
    require(index != -1) { "Replacement with id $itemId does not exist." }
    replacements.removeAt(index)
  }

  override suspend fun getReplacementById(itemId: String): Replacement? {
    return replacements.find { it.id == itemId }
  }

  override suspend fun getReplacementsByAbsentUser(userId: String): List<Replacement> {
    return replacements.filter { it.absentUserId == userId }
  }

  override suspend fun getReplacementsBySubstituteUser(userId: String): List<Replacement> {
    return replacements.filter { it.substituteUserId == userId }
  }

  override suspend fun getReplacementsByStatus(status: ReplacementStatus): List<Replacement> {
    return replacements.filter { it.status == status }
  }
}
