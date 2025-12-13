package com.android.sample.model.filter

import androidx.compose.ui.graphics.Color
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepository

class FakeEventCategoryRepository : EventCategoryRepository {

  var categories = listOf("Course", "Meeting", "Workshop")

  override suspend fun getAllCategories(orgId: String): List<EventCategory> {
    return categories.map { EventCategory(organizationId = orgId, label = it, color = Color.Red) }
  }

  override fun getNewUid(): String = "fake"

  override suspend fun insertCategory(orgId: String, item: EventCategory) {}

  override suspend fun updateCategory(orgId: String, itemId: String, item: EventCategory) {}

  override suspend fun deleteCategory(orgId: String, itemId: String) {}

  override suspend fun getCategoryById(orgId: String, itemId: String): EventCategory? = null
}
