package com.android.sample.model.eventCategoryRepositoryTest

import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepository
import com.android.sample.model.category.EventCategoryRepositoryProvider
import org.junit.Assert.assertSame
import org.junit.Test

// Assisted by AI

/**
 * Unit tests for [EventCategoryRepositoryProvider].
 *
 * Verifies that the repository instance can be replaced and retrieved correctly.
 */
class EventCategoryRepositoryProviderTest {

  @Test
  fun repository_returns_the_instance_we_set() {
    val fake =
        object : EventCategoryRepository {
          override fun getNewUid(): String = ""

          override suspend fun getAllCategories(orgId: String): List<EventCategory> = emptyList()

          override suspend fun deleteCategory(orgId: String, itemId: String) {}

          override suspend fun getCategoryById(orgId: String, itemId: String): EventCategory? = null
        }

    // Replace the default repository with the fake
    EventCategoryRepositoryProvider.repository = fake

    // Ensure the provider now returns the same instance
    assertSame(fake, EventCategoryRepositoryProvider.repository)
  }
}
