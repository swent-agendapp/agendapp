package com.android.sample.model.replacement

import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Unit tests for [ReplacementRepositoryProvider].
 *
 * Verifies that the repository instance can be replaced and retrieved correctly.
 */
class ReplacementRepositoryProviderTest {

  @Test
  fun repository_returns_the_instance_we_set() {
    val fake =
        object : ReplacementRepository {
          override suspend fun getAllReplacements(): List<Replacement> = emptyList()

          override suspend fun insertReplacement(item: Replacement) {}

          override suspend fun updateReplacement(itemId: String, item: Replacement) {}

          override suspend fun deleteReplacement(itemId: String) {}

          override suspend fun getReplacementById(itemId: String): Replacement? = null

          override suspend fun getReplacementsByAbsentUser(userId: String): List<Replacement> =
              emptyList()

          override suspend fun getReplacementsBySubstituteUser(userId: String): List<Replacement> =
              emptyList()

          override suspend fun getReplacementsByStatus(
              status: ReplacementStatus
          ): List<Replacement> = emptyList()
        }

    // Replace the default repository with the fake
    ReplacementRepositoryProvider.repository = fake

    // Ensure the provider now returns the same instance
    assertSame(fake, ReplacementRepositoryProvider.repository)
  }
}
