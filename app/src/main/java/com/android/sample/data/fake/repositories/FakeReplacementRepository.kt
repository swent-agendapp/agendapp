package com.android.sample.data.fake.repositories

import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementRepository
import com.android.sample.model.replacement.ReplacementStatus

class FakeReplacementRepository : ReplacementRepository {

  private val storage = mutableListOf<Replacement>()

  override suspend fun getAllReplacements(orgId: String): List<Replacement> = storage.toList()

  override suspend fun insertReplacement(orgId: String, item: Replacement) {
    storage.add(item)
  }

  override suspend fun updateReplacement(orgId: String, itemId: String, item: Replacement) {
    val idx = storage.indexOfFirst { it.id == itemId }
    if (idx != -1) storage[idx] = item
  }

  override suspend fun deleteReplacement(orgId: String, itemId: String) {
    storage.removeAll { it.id == itemId }
  }

  override suspend fun getReplacementById(orgId: String, itemId: String): Replacement? =
      storage.find { it.id == itemId }

  override suspend fun getReplacementsByAbsentUser(
      orgId: String,
      userId: String
  ): List<Replacement> = storage.filter { it.absentUserId == userId }

  override suspend fun getReplacementsBySubstituteUser(
      orgId: String,
      userId: String
  ): List<Replacement> = storage.filter { it.substituteUserId == userId }

  override suspend fun getReplacementsByStatus(
      orgId: String,
      status: ReplacementStatus
  ): List<Replacement> = storage.filter { it.status == status }
}
