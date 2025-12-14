package com.android.sample.model.filter

import androidx.compose.ui.graphics.Color
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepository

// Assisted by AI

/**
 * Fake implementation of [EventCategoryRepository] used for unit tests.
 *
 * This repository provides a fixed, in-memory list of event categories and supports **read-only**
 * operations required by tests.
 *
 * Write operations (insert, update, delete) are intentionally not supported and will throw
 * [UnsupportedOperationException] if called. This helps:
 * - Keep test logic simple and deterministic
 * - Ensure tests fail fast if unsupported operations are mistakenly used
 *
 * This fake is mainly used by [FilterViewModelTest] and similar unit tests that only require
 * category labels.
 */
class FakeEventCategoryRepository : EventCategoryRepository {

  var categories = listOf("Course", "Meeting", "Workshop")

  override suspend fun getAllCategories(orgId: String): List<EventCategory> {
    return categories.map { EventCategory(organizationId = orgId, label = it, color = Color.Red) }
  }

  override fun getNewUid(): String = "fake"

  override suspend fun insertCategory(orgId: String, item: EventCategory) {
    // Not needed for tests: Fake repository only supports read operations
  }

  override suspend fun updateCategory(orgId: String, itemId: String, item: EventCategory) {
    // Not needed for tests: Fake repository only supports read operations
  }

  override suspend fun deleteCategory(orgId: String, itemId: String) {
    // Not needed for tests: Fake repository only supports read operations
  }

  override suspend fun getCategoryById(orgId: String, itemId: String): EventCategory? {
    // Not needed for tests
    return null
  }
}
