package com.android.sample.model.replacement

import org.junit.Assert.assertSame
import org.junit.Test

// Assisted by AI

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
          override suspend fun getAllReplacements(orgId: String): List<Replacement> = emptyList()

          override suspend fun insertReplacement(orgId: String, item: Replacement) {}

          override suspend fun updateReplacement(
              orgId: String,
              itemId: String,
              item: Replacement
          ) {}

          override suspend fun deleteReplacement(orgId: String, itemId: String) {}

          override suspend fun getReplacementById(orgId: String, itemId: String): Replacement? =
              null

          override suspend fun getReplacementsByAbsentUser(
              orgId: String,
              userId: String
          ): List<Replacement> = emptyList()

          override suspend fun getReplacementsBySubstituteUser(
              orgId: String,
              userId: String
          ): List<Replacement> = emptyList()

          override suspend fun getReplacementsByStatus(
              orgId: String,
              status: ReplacementStatus
          ): List<Replacement> = emptyList()
        }

    // Replace the default repository with the fake
    ReplacementRepositoryProvider.repository = fake

    // Ensure the provider now returns the same instance
    assertSame(fake, ReplacementRepositoryProvider.repository)
  }
}
